package actors.clients

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import play.api.libs.json.{JsString, JsValue}
import model.common.data.MoneyTransaction

object CageClientActor {
  val name = "seater-admin-actor"
  val path = s"/usr/$name"

  def props(out: ActorRef,
            supervisor: ActorRef,
            remoteAddress: String
           ): Props = Props(new CageClientActor(out = out, supervisor = supervisor, remoteAddress = remoteAddress))
}

class CageClientActor(out: ActorRef, supervisor: ActorRef, remoteAddress: String)
  extends Actor
    with ActorLogging {

  import actors.MainActor._

  private var clientIp = remoteAddress

  override def preStart(): Unit = {
    if (clientIp != "") supervisor ! CageClientConnected(clientIp, self, out)
    super.preStart()
  }

  override def postStop(): Unit = {
    if (clientIp != "") supervisor ! CageClientDisconnected(clientIp);
    super.postStop()
  }


  override def receive: Receive = {
    case pingMsg: JsString =>
    case clientMsg: JsValue =>
      clientMsg("MessageType") match {
        case JsString("INITIALIZE") => {
          log.info(s"INITIALIZE received from ${clientIp}!")
          supervisor ! CageClientInitDataRequest(clientIp)
        }

        case JsString("DEPOSIT_REQ") => {
          val playerIp = clientMsg("clientIp").validate[String].fold(
            invalid = { fieldErrors =>
              fieldErrors.foreach { x =>
                log.info(s"field: ${x._1}, errors: ${x._2}")
              }
              ""
            },
            valid = { data =>
              data
            }
          )
          val uid = clientMsg("uid").validate[String].fold(
            invalid = { fieldErrors =>
              fieldErrors.foreach { x =>
                log.info(s"field: ${x._1}, errors: ${x._2}")
              }
              "-1"
            },
            valid = { data =>
              data
            }
          )
          val amount: Double = clientMsg("amount").validate[Double].fold(
            invalid = { fieldErrors =>
              fieldErrors.foreach { x =>
                log.info(s"field: ${x._1}, errors: ${x._2}")
              }
              0
            },
            valid = { data =>
              data
            }
          )

          log.info(s"DEPOSIT_REQ ${amount}  for ${uid} received from ${clientIp}!")

          if((uid != "-1") && (amount > 0) ) {
            supervisor ! CageClientMoneyTransaction(
              MoneyTransaction(
                transType = "DEPOSIT",
                admin = clientIp,
                playerIp = playerIp,
                uid = uid,
                amount = amount,
              ),
              out
            )
          }

        }
        case JsString("WITHDRAW_REQ") => {
          val playerIp = clientMsg("clientIp").validate[String].fold(
            invalid = { fieldErrors =>
              fieldErrors.foreach { x =>
                log.info(s"field: ${x._1}, errors: ${x._2}")
              }
              ""
            },
            valid = { data =>
              data
            }
          )
          val uid = clientMsg("uid").validate[String].fold(
            invalid = { fieldErrors =>
              fieldErrors.foreach { x =>
                log.info(s"field: ${x._1}, errors: ${x._2}")
              }
              "-1"
            },
            valid = { data =>
              data
            }
          )
          val amount: Double = clientMsg("amount").validate[Double].fold(
            invalid = { fieldErrors =>
              fieldErrors.foreach { x =>
                log.info(s"field: ${x._1}, errors: ${x._2}")
              }
              0
            },
            valid = { data =>
              data
            }
          )

          log.info(s"WITHDRAW_REQ ${amount}  for ${uid}  received from ${clientIp}!")

          if ((uid != "-1") && (amount > 0)) {
            supervisor ! CageClientMoneyTransaction(
              MoneyTransaction(
                transType = "WITHDRAW",
                admin = clientIp,
                playerIp = playerIp,
                uid = uid,
                amount = amount
              ),
              out
            )
          }

        }

        case _ => log.info(s"Unknown MessageType ${clientMsg("MessageType")} Received!")
      }

    case _ => log.error("Message unknown to CageClientActor!")
  }
}

