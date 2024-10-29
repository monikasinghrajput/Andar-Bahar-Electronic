package actors.ab.clients

//Standard Packages
import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import play.api.libs.json.{JsString, JsValue}
import model.ab.data.{BetsList, SideBet}
import actors.LogManagerActor.AddLog
import model.ab.ABJsonCodecs

object ClientActor {

  def props(out: ActorRef, dealer: ActorRef, logManagerActor: ActorRef, remoteAddress: String): Props =
    Props(new ClientActor(out, dealer, logManagerActor, remoteAddress))
}

class ClientActor(out: ActorRef,
                  dealer: ActorRef,
                  logManagerActor: ActorRef,
                  remoteAddress: String)
  extends Actor with ABJsonCodecs  {

  import actors.ab.AndarBaharTableActor._

  val log: Logger = Logger(this.getClass)



  private val clientIp = remoteAddress

  override def preStart(): Unit = {
    logManagerActor ! AddLog(logType = "warning", content = s"Client Socket Flow Actor for $clientIp Started")
    super.preStart()
  }

  override def postStop(): Unit = {
    logManagerActor ! AddLog(logType = "warning", content = s"Client Socket Flow Actor for $clientIp Stopped")
    if (clientIp != "") dealer ! PlayerDisConnected(clientIp)
    super.postStop()
  }

  override def receive: Receive = {
    case clientMsg: JsValue =>
      if (clientMsg == JsString("PingMessage")) {
      } else {
        clientMsg("MessageType") match {
          case JsString("INITIALIZE_PLAYER") =>
            log.logger.info(s"Player Socket Connect Request received from $clientIp!!")
            dealer ! PlayerConnected(clientIp, self, out)

          case JsString("PlaceBet") =>
            clientMsg("BetsList").validate[BetsList].fold(
              invalid = {fieldsErrors =>
                /*log each fieldErrors during decode for debug*/
                fieldsErrors.foreach{ fieldsError =>
                  log.warn(s"field=${fieldsError._1} error=${fieldsError._2.toString()}")
                }
                /*Decoding Failed, so..  */
                log.warn(s"BetsList Decoding Failed...${clientMsg("BetsList").toString()}")
              },
              valid = {betList =>
                clientMsg("SideBets").validate[SideBet].fold(
                  invalid = { fieldsErrors =>
                    /*log each fieldErrors during decode for debug*/
                    fieldsErrors.foreach { fieldsError =>
                      log.warn(s"field=${fieldsError._1} error=${fieldsError._2.toString()}")
                    }
                    /*Decoding Failed, so..  */
                    log.warn(s"SideBets Decoding Failed...${clientMsg("SideBets").toString()}")
                  },
                  valid = { sideBet =>
                    val combinedBetList = betList.copy(SideBets = sideBet)
                    log.warn(s"PlaceBet Decoded...${betList.toString} ${sideBet.toString}")
                    dealer ! PlayerBetPlaced(clientIp, combinedBetList, out)
                  }
                )
              }
            )

          //          case JsString("PlaceBetIntent") =>
//            val betList = clientMsg("BetsList").validate[BetsList].fold(
//              invalid = { fieldsErrors =>
//                /*log each fieldErrors during decode for debug*/
//                fieldsErrors.foreach { fieldsError =>
//                  log.warn(s"field=${fieldsError._1} error=${fieldsError._2.toString()}")
//                }
//                /*Decoding Failed, so..  */
//                log.warn(s"PlaceBetIntent Decoding Failed...${clientMsg("BetsList").toString()}")
//              },
//              valid = { betList =>
//                log.warn(s"PlaceBetIntent Decoded...${betList.toString}")
//                dealer ! PlayerBetIntentPlaced(clientIp, betList, out)
//              }
//            )

          case _ => log.logger.info(s"Unknown MessageType ${clientMsg("MessageType")} Received!")
        }
      }

    case _ => log.logger.info("Unknown Message Received!")

  }

}



