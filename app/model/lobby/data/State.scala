package model.lobby.data
import akka.actor.ActorRef
import model.common.CommonJsonCodecs
import model.common.data.{Player, ServerLog}
import play.api.libs.json.Json
import model.common.messages._
import actors.MainActor.PlayerBalanceUpdated

import java.text.SimpleDateFormat
import java.util.Calendar
import collection.mutable.{Map => MutableMap}

case class State(
                  clients: MutableMap[String, Client] = MutableMap.empty[String, Client],
                  tables: MutableMap[String, ActorRef] = MutableMap.empty[String, ActorRef],
                )
extends CommonJsonCodecs{

  val dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss.SSS z")

  def sendPlayerCreatedMsgToClients(player: Player): Unit = {
    val playerCreatedMsg = PlayerCreatedMsg(
      MessageType = "PLAYER_CREATED",
      player = player,
      timestamp = dateFormat.format(Calendar.getInstance().getTime))

    clients.foreach {
      client =>
        client._2.out ! Json.toJson(playerCreatedMsg)
    }

  }

  def sendPlayerUpdatedMsgToClients(player: Player, MessageType: String): Unit = {
    val playerUpdatedMsg = PlayerUpdatedMsg(
      MessageType,
      player = player,
      timestamp = dateFormat.format(Calendar.getInstance().getTime))

    clients.foreach {
      client =>
        client._2.out ! Json.toJson(playerUpdatedMsg)
    }

  }

  def sendGameTransactionMsgToClients(gameTransactionMsg: GameTransactionMsg): Unit = {
    clients.foreach {
      client =>
        client._2.out ! Json.toJson(gameTransactionMsg)
    }
  }

  def sendRoundTransactionMsgToClients(roundTransactionMsg: RoundTransactionMsg): Unit = {
    clients.foreach {
      client =>
        client._2.out ! Json.toJson(roundTransactionMsg)
    }
  }


  def sendMoneyTransactionMsg(moneyTransactionMsg: MoneyTransactionMsg, out: ActorRef): Unit = {
    out ! Json.toJson(moneyTransactionMsg)
  }





  def sendPlayerUpdatedMsgToClients(playerUpdatedMsg: PlayerUpdatedMsg, out: ActorRef): Unit = {
        out ! Json.toJson(playerUpdatedMsg)
  }

  def sendPlayerBalanceToAllTables(uid: String, newBalance: Double): Unit = {
    tables.foreach{
      table =>
        table._2 ! PlayerBalanceUpdated(uid, newBalance)
    }
  }

  def sendInitialDataMsg(logs: Seq[ServerLog] = Seq.empty[ServerLog],
                         players: Seq[Player] = Seq.empty[Player],
                         moneyTransactions: Seq[MoneyTransactionMsg] = Seq.empty[MoneyTransactionMsg],
                         gameTransactions: Seq[GameTransactionMsg] = Seq.empty[GameTransactionMsg],
                         roundTransactions: Seq[RoundTransactionMsg] = Seq.empty[RoundTransactionMsg],
                         operations: Seq[OperationTransactionMsg] = Seq.empty[OperationTransactionMsg],
                         out: ActorRef): Unit = {


    val initialDataMessage = InitialDataCageMsg(
      logs = logs,
      players = players,
      moneyTransactions = moneyTransactions,
      gameTransactions = gameTransactions,
      roundTransactions = roundTransactions,
      operations = operations
    )

    out ! Json.toJson(initialDataMessage)

  }

}
