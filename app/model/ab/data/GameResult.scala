package model.ab.data

import model.common.data.GameCard

case class GameResult(roundId: Long = 0,
                      JokerCard: String = "SHUFFLE",
                      BaharCards: Seq[GameCard] = Seq.empty[GameCard],
                      AndarCards: Seq[GameCard] = Seq.empty[GameCard],
                      cardCount: Int = 0,
                      WinningHand: String = "")
