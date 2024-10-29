package model.ab.message

case class CardSqueezedMsg(MessageType: String = "CardSqueezed",
                           destination: String = "player", //
                           clientId: String = "",
                           roundId: Long = 0,
                           CardHand: String = "", //"player", "banker"
                           CardIndex: Int = 0, // 0, 1, 2
                           timestamp: String = ""
)
