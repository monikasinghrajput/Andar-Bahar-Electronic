package model.common.data

import akka.actor.ActorRef

case class AdminClientData(actor:ActorRef = null ,
                           client:ActorRef = null,
                           name: String = "0.0.0.0",
                           balance: Double = 0.0)
