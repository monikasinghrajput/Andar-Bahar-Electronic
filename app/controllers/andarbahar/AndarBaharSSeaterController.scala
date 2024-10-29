package controllers.andarbahar

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.{JsError, JsValue, Json, Reads}
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import actors.ab.AndarBaharTableActor.CardDrawn
import actors.ab.clients.{AdminActor, ClientActor, TopperActor}
import model.ab.Card
import services.{AndarBaharSSeaterTableService, GameService}

case class Draw(card: String)

class AndarBaharSSeaterController(components: ControllerComponents,
                                  actorSystem: ActorSystem,
                                  gameService: GameService,
                                  andarbaharSSeaterTableService: AndarBaharSSeaterTableService,
                                  mat: Materializer)
  extends AbstractController(components)
{

  val log = Logger(this.getClass)

  implicit val reads: Reads[Draw] = Json.reads[Draw]

  implicit val materializer = mat
  implicit val actorFactory = actorSystem

  /*
  * serving html5 pages /front-end interfaces
  * - player
  * - topper
  * - admin
  * */

  def sendPlayerPage: Action[AnyContent] = Action { request =>
    log.logger.info(s"sending andarbahar player page for ${request.remoteAddress}")
    Ok(views.html.pages.andarbahar.player())
  }

  def sendTopperPage: Action[AnyContent] = Action { request =>
    log.logger.info(s"sending andarbahar topper page for ${request.remoteAddress}")
    Ok(views.html.pages.andarbahar.topper())
  }

  def sendAdminPage: Action[AnyContent] = Action { request =>
    log.logger.info(s"sending andarbahar admin page for ${request.remoteAddress}")
    Ok(views.html.pages.andarbahar.admin())
  }

  /*
  * serving websocket requests with a 2 way Json Interface
  *  - player
  *  - topper
  *  - admin
  * */

  def player: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
    log.logger.info(s"Controller Serving player socket request from ${request.remoteAddress}");

    ActorFlow.actorRef { out =>
      ClientActor.props(out, andarbaharSSeaterTableService.getAndarBaharTableActor, gameService.getLoggingActor, request.remoteAddress)
    }
  }


  def topper: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
    log.logger.info(s"Controller Serving AndarBahar Client: topper socket request from ${request.remoteAddress}");

    ActorFlow.actorRef { out =>
      TopperActor.props(out, andarbaharSSeaterTableService.getAndarBaharTableActor, gameService.getLoggingActor, request.remoteAddress)
    }
  }

  def admin: WebSocket = WebSocket.accept[JsValue, JsValue] { request =>
    log.logger.info(s"Controller Serving admin socket request from ${request.remoteAddress}");

    ActorFlow.actorRef { out =>
      AdminActor.props(out, andarbaharSSeaterTableService.getAndarBaharTableActor, gameService.getLoggingActor, request.remoteAddress)
    }
  }


  /*
  * HTTP Rest API (Json as the Content)
  *   - sendInitialDataJson - HTTP GET
  *   - sendAuthenticateJson - HTTP GET
  *   - sendStreamsJson - HTTP GET
  *   - handleCardDrawn - POST
  * */

  def sendInitialDataJson: Action[AnyContent] = Action(
    Ok(andarbaharSSeaterTableService.getInitialDataJsonString).withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referrer, User-Agent, X-Auth-Token, X-Api-Key")
  )

  def sendAuthenticateJson: Action[AnyContent] = Action(
    Ok(andarbaharSSeaterTableService.authenticateJsonString).withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referrer, User-Agent, X-Auth-Token, X-Api-Key")
  )

  def sendStreamsJson: Action[AnyContent] = Action(
    Ok(andarbaharSSeaterTableService.sendStreamsJsonString).withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referrer, User-Agent, X-Auth-Token, X-Api-Key")
  )

  def sendPayoutJson: Action[AnyContent] = Action(
    Ok(Json.prettyPrint(
      Json.obj(
        "error_code" -> Json.toJson(0),
        "error_message" -> Json.toJson(""),
        "result" -> Json.obj(
          "game_id" -> Json.toJson("38"),
          "multipliers" -> Json.toJson(Array.empty[Int]),
          "payouts" -> Json.obj(
            "1" -> "1",
            "2" -> "0.9",
            "3" -> "2.5",
            "4" -> "3.5",
            "5" -> "4.5",
            "6" -> "3.5",
            "7" -> "14",
            "8" -> "24",
            "9" -> "49",
            "10" -> "119",
            "11" -> "1",
            "12" -> "1"),
        ),
      )
    )).withHeaders("Access-Control-Allow-Origin" -> "*",
      "Allow" -> "*",
      "Access-Control-Allow-Methods" -> "POST, GET, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept, Referrer, User-Agent, X-Auth-Token, X-Api-Key")
  )


  def handleCardDrawn: Action[JsValue] = Action(parse.json) { request =>
    log.info(s"${request.body} received")

    val drawCard = request.body.validate[Draw]
    drawCard.fold(
      errors => {
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      },
      draw => {
        val card: Card = Card.parseBeeTekCard(draw.card).getOrElse(Card())
        andarbaharSSeaterTableService.getAndarBaharTableActor ! CardDrawn(card)
        Ok(Json.obj("message" -> ("Card is Forwarded Successfully.")))
      }
    )
  }

}
