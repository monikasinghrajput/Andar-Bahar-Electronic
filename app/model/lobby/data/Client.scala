package model.lobby.data

import akka.actor.ActorRef

case class Client(actor: ActorRef, out: ActorRef)
