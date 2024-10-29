package model.common.messages

import model.common.data.{Player, ServerLog}

case class InitialDataCageMsg(MessageType: String = "InitialData",
                              logs: Seq[ServerLog] = Seq.empty[ServerLog],
                              players: Seq[Player] = Seq.empty[Player],
                              moneyTransactions: Seq[MoneyTransactionMsg] = Seq.empty[MoneyTransactionMsg],
                              gameTransactions: Seq[GameTransactionMsg] = Seq.empty[GameTransactionMsg],
                              roundTransactions: Seq[RoundTransactionMsg] = Seq.empty[RoundTransactionMsg],
                              operations: Seq[OperationTransactionMsg] = Seq.empty[OperationTransactionMsg]
                             )