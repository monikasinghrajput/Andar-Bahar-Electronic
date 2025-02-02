package model.ab

import baccarat.Ranking
import model.common.data.GameCard

//data type Suit - subs(Hearts,Diamonds,Clubs,Spades)
sealed trait Suit
sealed case class Hearts() extends Suit {override def toString: String = "h"}
sealed case class Diamonds() extends Suit {override def toString: String = "d"}
sealed case class Clubs() extends Suit {override def toString: String = "c"}
sealed case class Spades() extends Suit {override def toString: String = "s"}

//data type Rank(value) - subs(Two,Three,Four,Five,Six,Seven,Eight,Nine,Ten,Jack,Queen,King,Ace)
sealed abstract class Rank(val value: Int) {override def toString: String = s"$value"}

sealed case class Ace() extends Rank(1) {override def toString: String = "A"}
sealed case class King() extends Rank(0){override def toString: String = "K"}
sealed case class Queen() extends Rank(0){override def toString: String = "Q"}
sealed case class Jack() extends Rank(0){override def toString: String = "J"}
sealed case class Ten() extends Rank(0){override def toString: String = "10"}
sealed case class Nine() extends Rank(9)
sealed case class Eight() extends Rank(8)
sealed case class Seven() extends Rank(7)
sealed case class Six() extends Rank(6)
sealed case class Five() extends Rank(5)
sealed case class Four() extends Rank(4)
sealed case class Three() extends Rank(3)
sealed case class Two() extends Rank(2)


object Rank {
  implicit val acesLow: Ranking[Rank] =
    Ranking.by[Rank] {
      case Ace()   =>  1
      case Two()   =>  2
      case Three() =>  3
      case Four()  =>  4
      case Five()  =>  5
      case Six()   =>  6
      case Seven() =>  7
      case Eight() =>  8
      case Nine()  =>  9
      case Ten()   => 10
      case Jack()  => 11
      case Queen() => 12
      case King()  => 13
    }

  implicit val acesHigh: Ranking[Rank] =
    Ranking.by[Rank] {
      case Ace()   => 14
      case Two()   =>  2
      case Three() =>  3
      case Four()  =>  4
      case Five()  =>  5
      case Six()   =>  6
      case Seven() =>  7
      case Eight() =>  8
      case Nine()  =>  9
      case Ten()   => 10
      case Jack()  => 11
      case Queen() => 12
      case King()  => 13
    }
}

case class Card(suit: Suit = Clubs(), rank: Rank = Ace()) {
  override def toString: String = suit.toString + rank.toString
}

object Card {
  val cardRegex = "([a-z])([a-zA-Z0-9]*)".r
  val beetekCardRegex = "([a-zA-Z])([a-zA-Z0-9]*)".r

  def parseBeeTekCard(str: String): Option[Card] =
    str match {
      case beetekCardRegex(suit, rank) =>
        for {
          suit <- parseSuit(suit.toLowerCase)
          rank <- parseRank(rank)
        } yield Card(suit, rank)

      case _ => None
    }


  def parseCard(str: String): Option[Card] =
    str match {
      case cardRegex(suit, rank) =>
        for {
          suit  <- parseSuit(suit)
          rank <- parseRank(rank)
        } yield Card(suit, rank)

      case _ => None
    }
  def parseGameCard(str: String): GameCard =
    str match {
      case cardRegex(suit, "10") =>
        GameCard(CardName = s"t$suit", CardValue = 0)
      case cardRegex(suit, "J") =>
        GameCard(CardName = s"j$suit", CardValue = 0)
      case cardRegex(suit, "Q") =>
        GameCard(CardName = s"q$suit", CardValue = 0)
      case cardRegex(suit, "K") =>
        GameCard(CardName = s"k$suit", CardValue = 0)
      case cardRegex(suit, "A") =>
        GameCard(CardName = s"a$suit", CardValue = 1)
      case cardRegex(suit, rank) =>
        GameCard(CardName = s"$rank$suit", CardValue = parseGameRank(rank))
      case _ => GameCard(CardName = s"", CardValue = 0)
    }
  def parseGameCardSqueezed(str: String): GameCard =
    str match {
      case cardRegex(suit, "10") =>
        GameCard(CardName = s"t$suit", CardValue = 0, squeezed = true)
      case cardRegex(suit, "J") =>
        GameCard(CardName = s"j$suit", CardValue = 0, squeezed = true)
      case cardRegex(suit, "Q") =>
        GameCard(CardName = s"q$suit", CardValue = 0, squeezed = true)
      case cardRegex(suit, "K") =>
        GameCard(CardName = s"k$suit", CardValue = 0, squeezed = true)
      case cardRegex(suit, "A") =>
        GameCard(CardName = s"a$suit", CardValue = 1, squeezed = true)
      case cardRegex(suit, rank) =>
        GameCard(CardName = s"$rank$suit", CardValue = parseGameRank(rank), squeezed = true)
      case _ => GameCard(CardName = s"", CardValue = 0, squeezed = true)
    }

  def parseRank(str: String): Option[Rank] =
    str match {
      case "A" | "a"   => Some(Ace())
      case "2"   => Some(Two())
      case "3" => Some(Three())
      case "4"  => Some(Four())
      case "5"  => Some(Five())
      case "6"   => Some(Six())
      case "7" => Some(Seven())
      case "8" => Some(Eight())
      case "9"  => Some(Nine())
      case "10" | "t" | "T"   => Some(Ten())
      case "J" | "j"  => Some(Jack())
      case "Q" | "q" => Some(Queen())
      case "K" | "k" => Some(King())
      case _          => None
    }

  def parseSuit(str: String): Option[Suit] =
    str match {
      case "c"    => Some(Clubs())
      case "d" => Some(Diamonds())
      case "h"   => Some(Hearts())
      case "s"   => Some(Spades())
      case _             => None
    }
  def parseGameRank(str: String): Int =
    str match {
      case "A" | "a"   => 1
      case "2"   => 2
      case "3" => 3
      case "4"  => 4
      case "5"  => 5
      case "6"   => 6
      case "7" => 7
      case "8" => 8
      case "9"  => 9
      case "10" | "t"   => 0
      case "J" | "j"  => 0
      case "Q" | "q" => 0
      case "K" | "k" => 0
      case _          => 0
    }


  def apply(str: String) = {
    parseCard(str)
  }
  type Deck = List[Card]
  type Hand = List[Card]

  def suits: List[Suit] = List(Hearts(),Diamonds(),Clubs(),Spades())
  def ranks: List[Rank] = List(Ace(),King(),Queen(),Jack(),Ten(),Nine(),Eight(),Seven(),Six(),Five(),Four(),Three(),Two())


  // val acesHigh: Ranking[Card] = Ranking.by(card => Rank.acesHigh(card.rank))
  // val acesLow: Ranking[Card] = Ranking.by(card => Rank.acesLow(card.rank))

  // implicit val ordering: Ordering[Card] = acesHigh
}
