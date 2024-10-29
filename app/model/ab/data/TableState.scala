package model.ab.data

import akka.actor.ActorRef
import model.ab.ABJsonCodecs

import java.time.Instant
import collection.mutable.{Map => MMap}
import model.common.data.{AdminClientData, GameCard, LicenseData, Player, ServerLog}
import model.common.messages.{CurrentBalanceMessage, MoneyTransactionMsg, OperationTransactionMsg, PlayerCreatedMsg}
import model.ab.message.{CardMsg, ConfigUpdateMsg, GameResultMsg, InitialDataAdminMsg, InitialDataMsg, NoMoreBetsMsg, PlaceYourBetsMsg, ShuffleMsg}
import play.api.Logger
import play.api.libs.json.Json

import java.text.SimpleDateFormat
import java.util.Calendar

case class TableState(roundId: Long = 1,
                      WinningHand: String = "",
                      JokerCard: String = "SHUFFLE",
                      BaharCards: Seq[GameCard] = Seq.empty[GameCard],
                      AndarCards: Seq[GameCard] = Seq.empty[GameCard],
                      configData: ConfigData = ConfigData(tableName = "AB-000", autoPlay = true),
                      History: Seq[GameResult] = Seq.empty[GameResult],
                      licenseData: LicenseData = LicenseData(),
                      admins: MMap[String, AdminClientData] = MMap.empty[String, AdminClientData],
                      players: MMap[String, ClientData] = MMap.empty[String, ClientData],
                      toppers: MMap[String, ClientData] = MMap.empty[String, ClientData],


                      gameStatus: String = "CLOSED",
                      gameCards: Array[List[String]] = Array(List("Joker"), List("Bahar"), List("Andar")),
                      autoPlayCounter: Int = 0,
                      drawCardCounter: Int = 0,
                      turn: (Int, Int) = (1, 1)
                     )
  extends ABJsonCodecs {

//  import org.joda.time.format.DateTimeFormat
//  import org.joda.time.format.DateTimeFormatter
//

//  val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd")
//  val profitCodes = MMap(
//    "4260441857" -> ("2023-01-01", "2023-12-01")
//  )
//
//  var licenseValid = false;
//  var licenseFormatValid = false;

  val log: Logger = Logger(this.getClass)
  val dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss.SSS z")

  def sendCurrentBalanceMsg(client: ActorRef, uid: String, balance: Double): Unit = {
    val currentBalanceMessage = CurrentBalanceMessage(
      tableId = "32100",
      clientId = uid,
      roundId = roundId,
      gameType = "AndarBahar",
      roundTripStartTime = Instant.now.getEpochSecond,
      timestamp = "timestamp",
      balance = balance,
    )

    client ! Json.toJson(currentBalanceMessage)
  }


  def sendShuffleDeckMsg(client: ActorRef): Unit = {

    val shuffleMsg = ShuffleMsg(roundId = roundId)

    client ! Json.toJson(shuffleMsg)


  }

  def sendPlaceYourBetsMsg(client: ActorRef): Unit = {

    val placeYourBetsMsg = PlaceYourBetsMsg(
      destination = "topper",
      roundId = roundId,
      timestamp = Instant.now().toString,
      TimerTime = 10,
      TimerTimeLeft = 10
    )

    client ! Json.toJson(placeYourBetsMsg)

  }

  def sendNoMoreBetsMsg(client: ActorRef): Unit = {

    val noMoreBetsMsg = NoMoreBetsMsg(
      destination = "topper",
      roundId = roundId,
      timestamp = Instant.now().toString,
    )

    client ! Json.toJson(noMoreBetsMsg)


  }

  def sendGameResultMsg(client: ActorRef, winAmount: Double = 0 ): Unit = {

    val gameResultMsg = GameResultMsg(
      roundId = roundId,
      timestamp = Instant.now().toString,
      WinAmount = winAmount,
      GameResults = GameResult(
        roundId = roundId,
        JokerCard,
        BaharCards = BaharCards,
        AndarCards = AndarCards,
        WinningHand = WinningHand
      )
    )
    client ! Json.toJson(gameResultMsg)

  }


  def sendShuffleDeckMsgToClients(): Unit = {

    val shuffleMsg = ShuffleMsg(roundId = roundId)

    toppers.foreach {
      topper =>
        topper._2.client ! Json.toJson(shuffleMsg)
    }

    admins.foreach {
      admin =>
        admin._2.client ! Json.toJson(shuffleMsg)
    }

    players.foreach {
      player =>
        player._2.client ! Json.toJson(shuffleMsg)
    }

  }

  def sendPlayerCreatedMsgToClients(player: Player): Unit = {

    val playerCreatedMsg = PlayerCreatedMsg(MessageType = "PLAYER_CREATED", player = player, timestamp = dateFormat.format(Calendar.getInstance().getTime))

    admins.foreach {
      admin =>
        admin._2.client ! Json.toJson(playerCreatedMsg)
    }

  }

  def sendPlaceYourBetsMsgToClients(): Unit = {

    val placeYourBetsMsg = PlaceYourBetsMsg(
      destination = "topper",
      roundId = roundId,
      timestamp = Instant.now().toString,
      TimerTime = 10,
      TimerTimeLeft = 10
    )

    admins.foreach {
      admin =>
        admin._2.client ! Json.toJson(placeYourBetsMsg)
    }

    toppers.foreach {
      topper =>
        topper._2.client ! Json.toJson(placeYourBetsMsg)
    }

    players.foreach {
      player =>
        player._2.client ! Json.toJson(placeYourBetsMsg)
    }

  }

  def sendNoMoreBetsMsgToClients(): Unit = {

    val noMoreBetsMsg = NoMoreBetsMsg(
      destination = "topper",
      roundId = roundId,
      timestamp = Instant.now().toString,
    )

    admins.foreach {
      admin =>
        admin._2.client ! Json.toJson(noMoreBetsMsg)
    }

    toppers.foreach {
      topper =>
        topper._2.client ! Json.toJson(noMoreBetsMsg)
    }

    players.foreach {
      player =>
        player._2.client ! Json.toJson(noMoreBetsMsg)
    }

  }

  def sendGameResultMsgToClients(): Unit = {


    val gameResult = GameResult(
      roundId = roundId,
      JokerCard,
      BaharCards = BaharCards,
      AndarCards = AndarCards,
      WinningHand = WinningHand
    )

    admins.foreach {
      admin =>
        val gameResultMsg = GameResultMsg(
          destination = "admin",
          roundId = roundId,
          timestamp = Instant.now().toString,
          GameResults = gameResult
        )
        admin._2.client ! Json.toJson(gameResultMsg)

    }

    toppers.foreach {
      topper =>
        val gameResultMsg = GameResultMsg(
          destination = "topper",
          roundId = roundId,
          timestamp = Instant.now().toString,
          GameResults = gameResult
        )

        topper._2.client ! Json.toJson(gameResultMsg)
    }

  }

  def sendCardMsgToClients(cardHand: String, cardHandValue: Int, cardName: String, cardValue: Int): Unit = {

    val cardMsg = CardMsg(
      destination = "",
      roundId = roundId,
      CardHand = cardHand,
      CardHandValue = cardHandValue,
      CardName = cardName,
      CardValue = cardValue,
      timestamp = Instant.now().toString,
    )


    admins.foreach {
      admin =>
        admin._2.client ! Json.toJson(cardMsg)

    }

    toppers.foreach {
      topper =>
        topper._2.client ! Json.toJson(cardMsg)
    }

    players.foreach {
      player =>
        player._2.client ! Json.toJson(cardMsg)
    }

  }


  def sendConfigUpdateMsgToClients(): Unit = {

    val configUpdateMsg = ConfigUpdateMsg(configData = configData, timestamp = Instant.now().toString)


    admins.foreach {
      admin =>
        admin._2.client ! Json.toJson(configUpdateMsg)
    }

    toppers.foreach {
      topper =>
        topper._2.client ! Json.toJson(configUpdateMsg)
    }

    players.foreach {
      player =>
        player._2.client ! Json.toJson(configUpdateMsg)
    }


  }

  def sendInitialConfigMsg(client: ActorRef): Unit = {
    val initialConfigMsg = ConfigUpdateMsg(configData = configData, timestamp = Instant.now().toString)
    client ! Json.toJson(initialConfigMsg)
  }

  def sendInitialDataMsg(clientData: ClientData, destination: String): Unit = {


    val initialDataMessage = InitialDataMsg(
      destination = destination,
      clientId = clientData.uid,
      roundId = roundId,
      timestamp = Instant.now().toString,
      JokerCard = JokerCard,
      BaharCards = BaharCards,
      AndarCards = AndarCards,
      History = History,
    )

    clientData.client ! Json.toJson(initialDataMessage)

  }


  def sendInitialDataAdminMsg(players: Seq[Player] = Seq.empty[Player],
                              transactions: Seq[MoneyTransactionMsg] = Seq.empty[MoneyTransactionMsg],
                              operations: Seq[OperationTransactionMsg] = Seq.empty[OperationTransactionMsg],
                              logs: Seq[ServerLog] = Seq.empty[ServerLog],
                              clientData: AdminClientData,
                              destination: String = "admin"): Unit = {

    val initialDataAdminMessage = InitialDataAdminMsg(
      roundId = roundId,
      destination = destination,
      timestamp = Instant.now().toString,
      JokerCard = JokerCard,
      BaharCards = BaharCards,
      AndarCards = AndarCards,
      History = History,

      logs = logs,
      players = players,
      transactions = transactions,
      operations = operations,
    )

    clientData.client ! Json.toJson(initialDataAdminMessage)

  }
}
