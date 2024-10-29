package model.ab.message

import model.ab.data.{BetsList, GameResult, StatisticsData, Winner}
import model.common.data.GameCard


case class InitialDataMsg(MessageType: String = "InitialData",
                          tableId: String = "",
                          gameType: String = "AndarBahar",
                          destination: String = "admin",
                          clientId: String = "",
                          roundId: Long = 0,
                          timestamp: String = "",

                          JokerCard: String = "shuffle",
                          BaharCards: Seq[GameCard] = Seq.empty[GameCard],
                          AndarCards: Seq[GameCard] = Seq.empty[GameCard],
                          LastWinners: Seq[Winner] = Seq.empty[Winner],
                          PlayerBetOfThisRound: BetsList = BetsList(),
                          Statistics: StatisticsData = StatisticsData(),
                          History: Seq[GameResult] = Seq.empty[GameResult],
                          balance: Double = 0.0,
                          isOppositeBettingAllowed: Boolean = true)
