package model.common.messages

case class GameTransactionMsg(MessageType: String,
                              transType: String = "Bet",
                              tableId: String,
                              gameName: String,
                              roundId: Long = 999,
                              betDetails: String,
                              winDetails: String = "",
                              resultDetails: String = "",
                              uid: String,
                              rake: Double = 0.0,
                              amount: Double = 0,
                              oldBalance: Double = 0,
                              newBalance: Double = 0,
                              timestamp: String)
