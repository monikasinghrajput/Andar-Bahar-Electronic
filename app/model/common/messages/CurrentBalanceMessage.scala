package model.common.messages

import java.time.Instant

case class CurrentBalanceMessage(MessageType: String = "CURRENT_BALANCE",
                                 tableId: String = "",
                                 destination: String = "player",
                                 clientId: String = "",
                                 roundId: Long = 0,
                                 gameType: String = "",
                                 roundTripStartTime: Long = Instant.now.getEpochSecond,
                                 timestamp: String = "",
                                 balance: Double = 0,
                                 sessionCurrency: String = "INR")
