package model.common

import akka.actor.ActorRef
import model.ab.data.{ClientData, SqueezedCard, Winner}
import model.ab.message.{CardMsg, CardSqueezedMsg, NoMoreBetsMsg, PlaceYourBetsMsg, ShuffleMsg}
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsResult, JsValue, Json, Reads, Writes}
import model.common.data.{Chip, GameCard, LicenseData, Player, ServerLog}
import model.common.messages.{CurrentBalanceMessage, GameTransactionMsg, InitialDataCageMsg, MoneyTransactionMsg, OperationTransactionMsg, PlayerCreatedMsg, PlayerUpdatedMsg, RoundTransactionMsg}

trait CommonJsonCodecs {


  implicit val chipReads: Reads[Chip] = (
    (JsPath \ "color").read[String] and
      (JsPath \ "value").read[Int] and
      (JsPath \ "img").read[String] and
      (JsPath \ "default").read[Boolean]
    ) (Chip.apply _)

  implicit val chipWrites: Writes[Chip] = new Writes[Chip] {
    def writes(chip: Chip): JsValue = Json.obj(
      "color" -> chip.color,
      "value" -> chip.value,
      "img" -> chip.img,
      "default" -> chip.default,
    )
  }


  implicit val logWrites: Writes[ServerLog] = new Writes[ServerLog] {
    def writes(log: ServerLog): JsValue = Json.obj(
      "logType" -> log.logType,
      "runtimeClass" -> log.runtimeClass,
      "content" -> log.content,
      "timestamp" -> log.timestamp,
    )
  }
  implicit val logReads: Reads[ServerLog] = (
    (JsPath \ "logType").read[String] and
      (JsPath \ "runtimeClass").read[String] and
      (JsPath \ "content").read[String] and
      (JsPath \ "timestamp").read[String]
    ) (ServerLog.apply _)


  implicit val winnerReads: Reads[Winner] = (
      (JsPath \ "OperatorID").read[String] and
      (JsPath \ "WinAmount").read[Double] and
      (JsPath \ "Nickname").read[String]
    ) (Winner.apply _)

  implicit val winnerWrites: Writes[Winner] = new Writes[Winner] {
    def writes(winner: Winner): JsValue = Json.obj(
      "OperatorID" -> winner.OperatorID,
      "WinAmount" -> winner.WinAmount,
      "Nickname" -> winner.Nickname,
    )
  }

  implicit val playerWrites: Writes[Player] = new Writes[Player] {
    def writes(player: Player): JsValue = Json.obj(
      "client_ip" -> player.clientIp,
      "nickname" -> player.nickname,
      "currency" -> player.currency,
      "uid" -> player.uid,
      "status" -> player.status,
      "usage" -> player.usage,
      "balance" -> player.balance
    )
  }

  implicit val playerReads: Reads[Player] = (
    (JsPath \ "client_ip").read[String] and
      (JsPath \ "nickname").read[String] and
      (JsPath \ "currency").read[String] and
      (JsPath \ "uid").read[String] and
      (JsPath \ "status").read[String] and
      (JsPath \ "usage").read[String] and
      (JsPath \ "balance").read[Double]
    ) (Player.apply _)

  implicit val playerUpdateWrites: Writes[PlayerUpdatedMsg] = new Writes[PlayerUpdatedMsg] {
    def writes(playerUpdatedMsg: PlayerUpdatedMsg): JsValue = Json.obj(
      "MessageType" -> playerUpdatedMsg.MessageType,
      "player" -> playerUpdatedMsg.player,
      "timestamp" -> playerUpdatedMsg.timestamp,
    )
  }

  implicit val playerCreatedWrites: Writes[PlayerCreatedMsg] = new Writes[PlayerCreatedMsg] {
    def writes(playerCreatedMsg: PlayerCreatedMsg): JsValue = Json.obj(
      "MessageType" -> playerCreatedMsg.MessageType,
      "player" -> playerCreatedMsg.player,
      "timestamp" -> playerCreatedMsg.timestamp
    )
  }


  /* License*/
  implicit val licenseDataWrites: Writes[LicenseData] = (licenseData: LicenseData) => Json.obj(
    "name" -> licenseData.name,
    "client" -> licenseData.client,
    "install" -> licenseData.install,
    "macs" -> licenseData.macs,
    "validProductCode" -> licenseData.validProductCode,
    "validProfitCode" -> licenseData.validProfitCode,
    "toBeExpired" -> licenseData.toBeExpired,
    "profitCode" -> licenseData.profitCode,
    "productCode" -> licenseData.productCode
  )

  implicit val licenseDataReads: Reads[LicenseData] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "client").read[String] and
      (JsPath \ "install").read[String] and
      (JsPath \ "macs").read[List[String]] and
      (JsPath \ "validProductCode").read[Boolean] and
      (JsPath \ "validProfitCode").read[Boolean] and
      (JsPath \ "toBeExpired").read[Boolean] and
      (JsPath \ "productCode").read[String] and
      (JsPath \ "profitCode").read[String]
    ) (LicenseData.apply _)


  implicit val operationTransactionMsgReads: Reads[OperationTransactionMsg] = (
    (JsPath \ "transType").read[String] and
      (JsPath \ "MessageType").read[String] and
      (JsPath \ "uid").read[String] and
      (JsPath \ "client_ip").read[String] and
      (JsPath \ "nickname").read[String] and
      (JsPath \ "status").read[String] and
      (JsPath \ "usage").read[String] and
      (JsPath \ "timestamp").read[String]
    ) (OperationTransactionMsg.apply _)

  implicit val operationTransactionMsgWrites: Writes[OperationTransactionMsg] = new Writes[OperationTransactionMsg] {
    def writes(operationTransaction: OperationTransactionMsg): JsValue = Json.obj(
      "transType" -> operationTransaction.transType,
      "MessageType" -> operationTransaction.MessageType,
      "uid" -> operationTransaction.uid,
      "client_ip" -> operationTransaction.client_ip,
      "nickname" -> operationTransaction.nickname,
      "status" -> operationTransaction.status,
      "usage" -> operationTransaction.usage,
      "timestamp" -> operationTransaction.timestamp,
    )
  }


  implicit val moneyTransactionMsgReads: Reads[MoneyTransactionMsg] = (
      (JsPath \ "MessageType").read[String] and
      (JsPath \ "transBy").read[String] and
      (JsPath \ "uid").read[String] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "oldBalance").read[Double] and
      (JsPath \ "newBalance").read[Double] and
      (JsPath \ "timestamp").read[String]
    ) (MoneyTransactionMsg.apply _)

  implicit val moneyTransactionMsgWrites: Writes[MoneyTransactionMsg] = new Writes[MoneyTransactionMsg] {
    def writes(moneyTransaction: MoneyTransactionMsg): JsValue = Json.obj(
      "MessageType" -> moneyTransaction.MessageType,
      "transBy" -> moneyTransaction.transBy,
      "uid" -> moneyTransaction.uid,
      "amount" -> moneyTransaction.amount,
      "oldBalance" -> moneyTransaction.oldBalance,
      "newBalance" -> moneyTransaction.newBalance,
      "timestamp" -> moneyTransaction.timestamp,
    )
  }
  implicit val gameTransactionMsgReads: Reads[GameTransactionMsg] = (
      (JsPath \ "MessageType").read[String] and
      (JsPath \ "transType").read[String] and
      (JsPath \ "tableId").read[String] and
      (JsPath \ "gameName").read[String] and
      (JsPath \ "roundId").read[Long] and
      (JsPath \ "betDetails").read[String] and
      (JsPath \ "winDetails").read[String] and
      (JsPath \ "resultDetails").read[String] and
      (JsPath \ "uid").read[String] and
      (JsPath \ "rake").read[Double] and
      (JsPath \ "amount").read[Double] and
      (JsPath \ "oldBalance").read[Double] and
      (JsPath \ "newBalance").read[Double] and
      (JsPath \ "timestamp").read[String]
    ) (GameTransactionMsg.apply _)


  implicit val gameTransactionMsgWrites: Writes[GameTransactionMsg] = new Writes[GameTransactionMsg] {
    def writes(moneyTransaction: GameTransactionMsg): JsValue = Json.obj(
      "MessageType" -> moneyTransaction.MessageType,
      "transType" -> moneyTransaction.transType,
      "tableId" -> moneyTransaction.tableId,
      "gameName" -> moneyTransaction.gameName,
      "roundId" -> moneyTransaction.roundId,
      "betDetails" -> moneyTransaction.betDetails,
      "winDetails" -> moneyTransaction.winDetails,
      "resultDetails" -> moneyTransaction.resultDetails,
      "uid" -> moneyTransaction.uid,
      "rake" -> moneyTransaction.rake,
      "amount" -> moneyTransaction.amount,
      "oldBalance" -> moneyTransaction.oldBalance,
      "newBalance" -> moneyTransaction.newBalance,
      "timestamp" -> moneyTransaction.timestamp,
    )
  }

  implicit val roundTransactionMsgRead: Reads[RoundTransactionMsg] = (
    (JsPath \ "MessageType").read[String] and
      (JsPath \ "transType").read[String] and
      (JsPath \ "tableId").read[String] and
      (JsPath \ "gameName").read[String] and
      (JsPath \ "roundId").read[Long] and
      (JsPath \ "winningHand").read[String] and
      (JsPath \ "gameResult").read[String] and
      (JsPath \ "playersTotalBet").read[List[(String, Double)]] and
      (JsPath \ "playersTotalWin").read[List[(String, Double)]] and
      (JsPath \ "playerBetsList").read[String] and
      (JsPath \ "playerWonBetsList").read[String] and
      (JsPath \ "timestamp").read[String]
    ) (RoundTransactionMsg.apply _)

  implicit val roundTransactionMsgWrites: Writes[RoundTransactionMsg] = new Writes[RoundTransactionMsg] {
    def writes(roundTransactionMsg: RoundTransactionMsg): JsValue = Json.obj(
      "MessageType" -> roundTransactionMsg.MessageType,
      "transType" -> roundTransactionMsg.transType,
      "tableId" -> roundTransactionMsg.tableId,
      "gameName" -> roundTransactionMsg.gameName,
      "roundId" -> roundTransactionMsg.roundId,
      "winningHand" -> roundTransactionMsg.winningHand,
      "gameResult" -> roundTransactionMsg.gameResult,
      "playersTotalBet" -> roundTransactionMsg.playersTotalBet,
      "playersTotalWin" -> roundTransactionMsg.playersTotalWin,
      "playerBetsList" -> roundTransactionMsg.playerBetsList,
      "playerWonBetsList" -> roundTransactionMsg.playerWonBetsList,
      "timestamp" -> roundTransactionMsg.timestamp,
    )
  }

  implicit val currentBalanceMsgWrites: Writes[CurrentBalanceMessage] = new Writes[CurrentBalanceMessage] {
    def writes(currentBalanceMsg: CurrentBalanceMessage): JsValue = Json.obj(
      "MessageType" -> currentBalanceMsg.MessageType,
      "TableId" -> currentBalanceMsg.tableId,
      "destination" -> currentBalanceMsg.destination,
      "ClientId" -> currentBalanceMsg.clientId,
      "roundId" -> currentBalanceMsg.roundId,
      "gameType" -> currentBalanceMsg.gameType,
      "RoundTripStartTime" -> currentBalanceMsg.roundTripStartTime,
      "timestamp" -> currentBalanceMsg.timestamp,
      "balance" -> currentBalanceMsg.balance,
      "SessionCurrency" -> currentBalanceMsg.sessionCurrency
    )
  }

  implicit val shuffleMsgWrites: Writes[ShuffleMsg] = new Writes[ShuffleMsg] {
    def writes(shuffleMsg: ShuffleMsg): JsValue = Json.obj(
      "MessageType" -> shuffleMsg.MessageType,
      "destination" -> shuffleMsg.destination,
      "clientId" -> shuffleMsg.clientId,
      "roundId" -> shuffleMsg.roundId,
      "timestamp" -> shuffleMsg.timestamp,
    )
  }


  implicit val placeYourBetsMsgWrites: Writes[PlaceYourBetsMsg] = (
    (JsPath \ "MessageType").write[String] and
      (JsPath \ "destination").write[String] and
      (JsPath \ "clientId").write[String] and
      (JsPath \ "roundId").write[Long] and
      (JsPath \ "TimerTimeLeft").write[Int] and
      (JsPath \ "TimerTime").write[Int] and
      (JsPath \ "timestamp").write[String]
    ) (unlift(PlaceYourBetsMsg.unapply))

  implicit val noMoreBetsMsgWrites: Writes[NoMoreBetsMsg] = (
    (JsPath \ "MessageType").write[String] and
      (JsPath \ "destination").write[String] and
      (JsPath \ "clientId").write[String] and
      (JsPath \ "roundId").write[Long] and
      (JsPath \ "timestamp").write[String]
    ) (unlift(NoMoreBetsMsg.unapply))


  implicit val cardMsgWrites: Writes[CardMsg] = (
    (JsPath \ "MessageType").write[String] and
      (JsPath \ "destination").write[String] and
      (JsPath \ "clientId").write[String] and
      (JsPath \ "roundId").write[Long] and
      (JsPath \ "CardHand").write[String] and
      (JsPath \ "CardHandValue").write[Int] and
      (JsPath \ "CardName").write[String] and
      (JsPath \ "CardValue").write[Int] and
      (JsPath \ "squeezed").write[Boolean] and
      (JsPath \ "timestamp").write[String]
    ) (unlift(CardMsg.unapply))


  implicit val cardSqueezedMsgWrites: Writes[CardSqueezedMsg] = (
    (JsPath \ "MessageType").write[String] and
      (JsPath \ "destination").write[String] and
      (JsPath \ "clientId").write[String] and
      (JsPath \ "roundId").write[Long] and
      (JsPath \ "CardHand").write[String] and
      (JsPath \ "CardIndex").write[Int] and
      (JsPath \ "timestamp").write[String]
    ) (unlift(CardSqueezedMsg.unapply))



  implicit val gameCardWrites: Writes[GameCard] = new Writes[GameCard] {
    override def writes(o: GameCard): JsValue = Json.obj(
      "CardName" -> o.CardName,
      "CardValue" -> o.CardValue,
      "squeezed" -> o.squeezed
    )
  }

  implicit val gameCardReads: Reads[GameCard] = (
    (JsPath \ "CardName").read[String] and
      (JsPath \ "CardValue").read[Int] and
      (JsPath \ "squeezed").read[Boolean]
    ) (GameCard.apply _)


  implicit val squeezedCardWrites: Writes[SqueezedCard] = new Writes[SqueezedCard] {
    override def writes(o: SqueezedCard): JsValue = Json.obj(
      "hand" -> o.hand,
      "index" -> o.index,
    )
  }

  implicit val squeezedCardReads: Reads[SqueezedCard] = (
    (JsPath \ "hand").read[Int] and
      (JsPath \ "index").read[Int]
    ) (SqueezedCard.apply _)


  implicit val initialDataCageMsgWrites: Writes[InitialDataCageMsg] = new Writes[InitialDataCageMsg] {
    def writes(initialDataMsg: InitialDataCageMsg): JsValue = Json.obj(
      "MessageType" -> initialDataMsg.MessageType,
      "logs" -> initialDataMsg.logs,
      "players" -> initialDataMsg.players,
      "moneyTransactions" -> initialDataMsg.moneyTransactions,
      "gameTransactions" -> initialDataMsg.gameTransactions,
      "roundTransactions" -> initialDataMsg.roundTransactions,
      "operations" -> initialDataMsg.operations,
    )
  }
}
