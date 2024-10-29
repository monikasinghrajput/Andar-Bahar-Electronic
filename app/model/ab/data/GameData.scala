package model.ab.data

import model.common.data.GameCard

case class GameData(History: Seq[GameResult] = Seq.empty[GameResult],
                    ColdNumbers: Seq[Int] = Seq.empty[Int],
                    HotNumbers: Seq[Int] = Seq.empty[Int],
                    BaharCards: Seq[GameCard] = Seq.empty[GameCard],
                    AndarCards: Seq[GameCard] = Seq.empty[GameCard],
                    bankerHandValue: Int = 0,
                    playerHandValue: Int = 0,
                    playerBetOfThisRound: BetsList = BetsList())
