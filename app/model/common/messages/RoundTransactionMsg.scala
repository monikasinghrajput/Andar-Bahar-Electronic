package model.common.messages

case class RoundTransactionMsg(MessageType: String,
                               transType: String,
                               tableId: String,
                               gameName: String,
                               roundId: Long,
                               winningHand: String,
                               gameResult: String,
                               playersTotalBet: List[(String, Double)],
                               playersTotalWin: List[(String, Double)],
                               playerBetsList: String,
                               playerWonBetsList: String,
                               timestamp: String)
