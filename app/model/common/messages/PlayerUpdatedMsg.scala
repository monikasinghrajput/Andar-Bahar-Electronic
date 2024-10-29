package model.common.messages

import model.common.data.Player

case class PlayerUpdatedMsg(MessageType: String, player: Player, timestamp: String)
