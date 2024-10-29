package model.common.messages

case class MoneyTransactionMsg(MessageType: String,
                               transBy: String = "Cashier",
                               uid: String,
                               amount: Double = 0,
                               oldBalance: Double = 0,
                               newBalance: Double = 0,
                               timestamp: String)
