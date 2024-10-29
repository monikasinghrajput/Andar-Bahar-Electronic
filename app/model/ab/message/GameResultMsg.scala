package model.ab.message

import model.ab.data.GameResult

case class GameResultMsg(MessageType: String = "GAME_RESULT",//Important
                         destination: String = "player", //
                         clientId: String = "",
                         roundId: Long = 0,//Important
                         timestamp: String = "",//Important
                         WinAmount: Double = 0, //Important
                         GameResults: GameResult = GameResult() //Important
)
