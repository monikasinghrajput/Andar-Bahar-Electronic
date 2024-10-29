package model.ab.data

import akka.actor.ActorRef

case class ClientData(actor:ActorRef = null,
                      client:ActorRef = null,
                      uid : String = "-1",
                      playerIp: String = "192.168.0.1",
                      betsList: BetsList = BetsList(),
                      wonBetsList: BetsList = BetsList(),
                      balance: Double = 0.0)
