package model.common.data

case class MoneyTransaction(transType: String = "Undefined",
                            admin: String,
                            playerIp: String = "",
                            uid: String = "-1",
                            amount: Double)
