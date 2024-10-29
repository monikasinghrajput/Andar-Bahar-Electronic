package model.ab.data

case class GameTransaction(roundId: Long,
                           rake: Double = 0.0,
                           game: String = "",
                           transType: String = "Undefined",
                           player: String,
                           totalBet: Double = 0,
                           betList: BetsList = null,
                           totalWin: Double = 0,
                           wonList: BetsList = null,
                           oldBalance: Double = 0,
                           balance: Double = 0)
