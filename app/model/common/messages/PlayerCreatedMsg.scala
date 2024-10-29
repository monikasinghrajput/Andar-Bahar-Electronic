package model.common.messages

import model.common.data.Player

case class PlayerCreatedMsg(MessageType: String, player: Player, timestamp: String)
