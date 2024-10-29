package model.ab

import model.ab.data._
import model.ab.message._
import model.common.CommonJsonCodecs
import model.common.data.{AdminClientData, GameCard, LicenseData}

import collection.mutable.{Map => MMap}

trait ABJsonCodecs extends CommonJsonCodecs {


  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  /* For Admin Configuration Support for
  * ConfigData
  * 1.
  * 2.
  * 3.
  *  */


  implicit val tableBetLimitWrites: Writes[TableBetLimit] = new Writes[TableBetLimit] {
    override def writes(o: TableBetLimit): JsValue = Json.obj(
      "Min_Bet" -> o.Min_Bet,
      "Max_Bet" -> o.Max_Bet,
      "Min_Bet_2" -> o.Min_Bet_2,
      "Max_Bet_2" -> o.Max_Bet_2,
      "Min_Group1" -> o.Min_Group1,
      "Max_Group1" -> o.Max_Group1,
      "Min_Group2" -> o.Min_Group2,
      "Max_Group2" -> o.Max_Group2,
      "Min_Group3" -> o.Min_Group3,
      "Max_Group3" -> o.Max_Group3,
      "Min_Group4" -> o.Min_Group4,
      "Max_Group4" -> o.Max_Group4,
      "Min_Group5" -> o.Min_Group5,
      "Max_Group5" -> o.Max_Group5,
      "Min_Group6" -> o.Min_Group6,
      "Max_Group6" -> o.Max_Group6,
      "Min_Group7" -> o.Min_Group7,
      "Max_Group7" -> o.Max_Group7,
      "Min_Group8" -> o.Min_Group8,
      "Max_Group8" -> o.Max_Group8,
    )
  }

  implicit val tableLimitReads: Reads[TableBetLimit] = (
    (JsPath \ "Min_Bet").read[Int] and
      (JsPath \ "Max_Bet").read[Int] and
      (JsPath \ "Min_Bet_2").read[Int] and
      (JsPath \ "Max_Bet_2").read[Int] and
      (JsPath \ "Min_Group1").read[Int] and
      (JsPath \ "Max_Group1").read[Int] and
      (JsPath \ "Min_Group2").read[Int] and
      (JsPath \ "Max_Group2").read[Int] and
      (JsPath \ "Min_Group3").read[Int] and
      (JsPath \ "Max_Group3").read[Int] and
      (JsPath \ "Min_Group4").read[Int] and
      (JsPath \ "Max_Group4").read[Int] and
      (JsPath \ "Min_Group5").read[Int] and
      (JsPath \ "Max_Group5").read[Int] and
      (JsPath \ "Min_Group6").read[Int] and
      (JsPath \ "Max_Group6").read[Int] and
      (JsPath \ "Min_Group7").read[Int] and
      (JsPath \ "Max_Group7").read[Int] and
      (JsPath \ "Min_Group8").read[Int] and
      (JsPath \ "Max_Group8").read[Int]
    ) (TableBetLimit.apply _)


  implicit val configDataWrites: Writes[ConfigData] = new Writes[ConfigData] {
    override def writes(o: ConfigData): JsValue = Json.obj(
      "tableLimit" -> o.tableLimit,
      "tableName" -> o.tableName,
      "tableDifferential" -> o.tableDifferential,
      "showInfoPaper" -> o.showInfoPaper,
      "autoDraw" -> o.autoDraw,
      "autoPlay" -> o.autoPlay,
      "isOppositeBettingAllowed" -> o.isOppositeBettingAllowed,
    )
  }


  implicit val configDataReads: Reads[ConfigData] = (
    (JsPath \ "tableLimit").read[TableBetLimit] and
      (JsPath \ "tableName").read[String] and
      (JsPath \ "tableDifferential").read[Int] and
      (JsPath \ "showInfoPaper").read[Boolean] and
      ((JsPath \ "autoDraw").read[Boolean] or Reads.pure(false)) and
      ((JsPath \ "autoPlay").read[Boolean] or Reads.pure(false)) and
      ((JsPath \ "isOppositeBettingAllowed").read[Boolean] or Reads.pure(false))
    ) (ConfigData.apply _)


  implicit val configUpdateMsgWrites: Writes[ConfigUpdateMsg] = new Writes[ConfigUpdateMsg] {
    def writes(configUpdateMsg: ConfigUpdateMsg): JsValue = Json.obj(
      "MessageType" -> configUpdateMsg.MessageType,
      "configData" -> configUpdateMsg.configData,
      "timestamp" -> configUpdateMsg.timestamp,
    )
  }



  /* Bet Related Codec Start
  *
  *
  * */




  implicit val sideBetWrite: Writes[SideBet] = new Writes[SideBet] {
    override def writes(o: SideBet): JsValue = Json.obj(
      "Joker_1_5" -> o.Joker_1_5,
      "Joker_6_10" -> o.Joker_6_10,
      "Joker_11_15" -> o.Joker_11_15,
      "Joker_16_25" -> o.Joker_16_25,
      "Joker_26_30" -> o.Joker_26_30,
      "Joker_31_35" -> o.Joker_31_35,
      "Joker_36_40" -> o.Joker_36_40,
      "Joker_41" -> o.Joker_41,
    )
  }

  implicit val sideBetReads: Reads[SideBet] = (
    ((JsPath \ "Joker_1_5").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_6_10").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_11_15").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_16_25").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_26_30").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_31_35").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_36_40").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Joker_41").read[Double] or Reads.pure(0.0))
    ) (SideBet.apply _)


  implicit val betsListWrites: Writes[BetsList] = new Writes[BetsList] {
    override def writes(o: BetsList): JsValue = Json.obj(
      "AndarBet" -> o.AndarBet,
      "Andar2ndBet" -> o.Andar2ndBet,
      "BaharBet" -> o.BaharBet,
      "Bahar2ndBet" -> o.Bahar2ndBet,
      "SideBets" -> o.SideBets
    )
  }

  implicit val betsListReads: Reads[BetsList] = (
    ((JsPath \ "AndarBet").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Andar2ndBet").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "BaharBet").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "Bahar2ndBet").read[Double] or Reads.pure(0.0)) and
      ((JsPath \ "SideBets").read[SideBet] or Reads.pure(SideBet()))
    ) (BetsList.apply _)

  /* Bet Related Codec End*/


  implicit val gameResultWrites: Writes[GameResult] = new Writes[GameResult] {
    def writes(winResult: GameResult): JsValue = Json.obj(
      "roundId" -> winResult.roundId,
      "JokerCard" -> winResult.JokerCard,
      "BaharCards" -> winResult.BaharCards,
      "AndarCards" -> winResult.AndarCards,
      "cardCount" -> winResult.cardCount,
      "WinningHand" -> winResult.WinningHand
    )
  }

  implicit val gameResultReads: Reads[GameResult] = (
    (JsPath \ "roundId").read[Long] and
      (JsPath \ "JokerCard").read[String] and
      (JsPath \ "BaharCards").read[Seq[GameCard]] and
      (JsPath \ "AndarCards").read[Seq[GameCard]] and
      (JsPath \ "cardCount").read[Int] and
      (JsPath \ "WinningHand").read[String]
    ) (GameResult.apply _)



  implicit val jokerStatisticsDataWrites: Writes[JokerStatisticsData] = new Writes[JokerStatisticsData] {
    override def writes(o: JokerStatisticsData): JsValue = Json.obj(
      "Bahar" -> o.Bahar,
      "Andar" -> o.Andar,
    )
  }
  implicit val winPercentagesData: Writes[WinPercentages] = new Writes[WinPercentages] {
    override def writes(o: WinPercentages): JsValue = Json.obj(
      "Bahar" -> o.Bahar,
      "Andar" -> o.Andar,
    )
  }

  implicit val statisticsDatasData: Writes[StatisticsData] = new Writes[StatisticsData] {
    override def writes(o: StatisticsData): JsValue = Json.obj(
      "JokerStatistics" -> o.JokerStatistics,
      "winPercentages" -> o.winPercentages,
      "lastWinners" -> o.lastWinners,
      "lastJokers" -> o.lastJokers,
    )
  }



  implicit val initialDataAdminMsgWrites: Writes[InitialDataAdminMsg] = new Writes[InitialDataAdminMsg] {
    def writes(initialDataMsg: InitialDataAdminMsg): JsValue = Json.obj(
      "MessageType" -> initialDataMsg.MessageType,
      "TableId" -> initialDataMsg.tableId,
      "gameType" -> initialDataMsg.gameType,
      "destination" -> initialDataMsg.destination,
      "clientId" -> initialDataMsg.clientId,
      "roundId" -> initialDataMsg.roundId,
      "timestamp" -> initialDataMsg.timestamp,

      "JokerCard" -> initialDataMsg.JokerCard,
      "BaharCards" -> initialDataMsg.BaharCards,
      "AndarCards" -> initialDataMsg.AndarCards,
      "LastWinners" -> initialDataMsg.LastWinners,
      "PlayerBetOfThisRound" -> initialDataMsg.PlayerBetOfThisRound,
      "Statistics" -> initialDataMsg.Statistics,
      "History" -> initialDataMsg.History,

      "balance" -> initialDataMsg.balance,
      "isOppositeBettingAllowed" -> initialDataMsg.isOppositeBettingAllowed,

      "logs" -> initialDataMsg.logs,
      "players" -> initialDataMsg.players,
      "transactions" -> initialDataMsg.transactions,
      "operations" -> initialDataMsg.operations,
    )
  }

  implicit val initialDataMsgWrites: Writes[InitialDataMsg] = new Writes[InitialDataMsg] {
    def writes(initialDataMsg: InitialDataMsg): JsValue = Json.obj(
      "MessageType" -> initialDataMsg.MessageType,
      "TableId" -> initialDataMsg.tableId,
      "gameType" -> initialDataMsg.gameType,
      "destination" -> initialDataMsg.destination,
      "clientId" -> initialDataMsg.clientId,
      "roundId" -> initialDataMsg.roundId,
      "timestamp" -> initialDataMsg.timestamp,

      "JokerCard" -> initialDataMsg.JokerCard,
      "BaharCards" -> initialDataMsg.BaharCards,
      "AndarCards" -> initialDataMsg.AndarCards,
      "LastWinners" -> initialDataMsg.LastWinners,
      "PlayerBetOfThisRound" -> initialDataMsg.PlayerBetOfThisRound,
      "Statistics" -> initialDataMsg.Statistics,
      "History" -> initialDataMsg.History,

      "balance" -> initialDataMsg.balance,
      "isOppositeBettingAllowed" -> initialDataMsg.isOppositeBettingAllowed,
    )
  }


  implicit val gameResultMsgWrites: Writes[GameResultMsg] = (
    (JsPath \ "MessageType").write[String] and
      (JsPath \ "destination").write[String] and
      (JsPath \ "clientId").write[String] and
      (JsPath \ "roundId").write[Long] and
      (JsPath \ "timestamp").write[String] and
      (JsPath \ "WinAmount").write[Double] and
      (JsPath \ "GameResults").write[GameResult]
    ) (unlift(GameResultMsg.unapply))

  implicit val wonBetsMsgWrites: Writes[WonBetsMsg] = new Writes[WonBetsMsg] {
    override def writes(o: WonBetsMsg): JsValue = Json.obj(
      "MessageType" -> o.MessageType,
      "destination" -> o.destination,
      "clientId" -> o.clientId,
      "RoundTripStartTime" -> o.RoundTripStartTime,
      "WinningBets" -> o.WinningBets,
    )
  }

}
