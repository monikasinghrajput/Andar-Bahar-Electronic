package actors.ab.clients

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.{JsString, JsValue}
import model.ab.{ABJsonCodecs, Card}
import model.ab.data.{ConfigData, GameResult}

object AdminActor {
  val name = "baccarat-stadium-seater-admin-actor"
  val path = s"/usr/$name"

  def props(out: ActorRef,
            dealer: ActorRef,
            logActor: ActorRef,
            remoteAddress: String
  ): Props = Props(new AdminActor(out = out, dealer = dealer, logActor = logActor, remoteAddress = remoteAddress))
}

class AdminActor(out: ActorRef, dealer: ActorRef, logActor: ActorRef, remoteAddress: String)
  extends Actor
    with ABJsonCodecs
    with ActorLogging {

  import actors.ab.AndarBaharTableActor._

  private val clientIp = remoteAddress

  override def preStart(): Unit = super.preStart()

  override def postStop(): Unit = {
    if (clientIp != "") dealer ! AdminDisConnected(clientIp)
    super.postStop()
  }


  override def receive: Receive = {
    case pingMsg: JsString =>
    case clientMsg: JsValue =>
        clientMsg("MessageType") match {
          case JsString("INITIALIZE_ADMIN") =>
            log.info(s"AB INITIALIZE_ADMIN received from $clientIp!")
            dealer ! AdminConnected(clientIp, self, out)


          case JsString("CONFIG_UPDATE") =>
            val configData = clientMsg("configData").validate[ConfigData].fold(
              invalid = { fieldErrors =>
                log.info(s"CONFIG_UPDATE Decoding failed..")
                fieldErrors.foreach { x =>
                  log.info(s"field: ${x._1}, errors: ${x._2}")
                }
                ConfigData()
              },
              valid = { data =>
                log.info(s"CONFIG_UPDATE Decoding Success..")
                data
              }
            )
            log.info(s"Config Data Decoded => ${configData.toString}")
            if (configData.tableName != "EMPTY") dealer ! ConfigUpdateCommand(configData)

          /*Baccarat Admin Special Commands*/
          case JsString("INFO_TURN_ON_COMMAND") =>
            dealer ! InfoPaperShow(true)
          case JsString("INFO_TURN_OFF_COMMAND") =>
            dealer ! InfoPaperShow(false)

          case JsString("RESET_GAME_COMMAND") =>
            dealer ! ShuffleDeck
          case JsString("START_GAME_COMMAND") =>
            dealer ! ShuffleDeck

//          case JsString("CLEAR_SHOE_COMMAND") =>
//            dealer ! ShuffleDeck

//          case JsString("REMOVE_PREV_RESULT_COMMAND") =>
//            dealer ! CancelPrevGame

//          case JsString("TOGGLE_AUTO_DRAW") =>
//            dealer ! ToggleAutoDraw

          case JsString("TOGGLE_AUTO_PLAY") =>
            dealer ! AutoPlayToggleCommand

          case JsString("CARD_DRAWN") =>
            log.info(s"Card Drawn is ${clientMsg("card")}")
            val baccaratCard: Card = Card.parseBeeTekCard(clientMsg("card").validate[String].get).getOrElse(Card())
            dealer ! CardDrawn(baccaratCard)

          case JsString("GAME_RESULT") =>
            log.info(s"GAME_RESULT received ${clientMsg("winResult")}")
            val winResult = clientMsg("winResult").validate[GameResult].fold(
              invalid = { fieldErrors =>
                fieldErrors.foreach { x =>
                  log.info(s"field: ${x._1}, errors: ${x._2}")
                }
                GameResult()
              },
              valid = { data =>
                data
              }
            )

            val cardCount = clientMsg("cardCount").validate[Int].fold(
              invalid = { fieldErrors =>
                fieldErrors.foreach { x =>
                  log.info(s"field: ${x._1}, errors: ${x._2}")
                }
                1
              },
              valid = { data =>
                data
              }
            )
            log.info(s"Win Result Decoded => ${winResult.toString} count=${cardCount}")
            if(winResult.roundId != -1) dealer ! WinResultMsgCommand(winResult, cardCount)


        case _ => log.info(s"Unknown MessageType ${clientMsg("MessageType")} Received!")
      }
    case _ => log.info("Unknown Message Received!")
  }
}
