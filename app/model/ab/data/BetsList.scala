package model.ab.data

//val ANDAR = 'AndarBet';
//val ANDAR_2 = 'Andar2ndBet';
//val BAHAR = 'BaharBet';
//val BAHAR_2 = 'Bahar2ndBet';
//val ONE_TO_FIVE = 'Joker_1_5';
//val SIX_TO_TEN = 'Joker_6_10';
//val ELEVEN_TO_FIFTEEN = 'Joker_11_15';
//val SIXTEEN_TO_TWENTYFIVE = 'Joker_16_25';
//val TWENTYSIX_TO_THIRTY = 'Joker_26_30';
//val THIRTYONE_TO_THIRTYFIVE = 'Joker_31_35';
//val THIRTYSIX_TO_FOURTY = 'Joker_36_40';
//val FOURTYONE_OR_MORE = 'Joker_41';
//val PAIR_PLUS = 'Pair_Plus';
//val ANDAR_FIRST = 'AndarFirstBet';
//val ANDAR_SECOND = 'AndarSecondBet';
//val BAHAR_FIRST = 'BaharFirstBet';
//val BAHAR_SECOND = 'BaharSecondBet';

case class BetsList(AndarBet: Double = 0,
                    Andar2ndBet: Double = 0,
                    BaharBet: Double = 0,
                    Bahar2ndBet: Double = 0,
                    SideBets: SideBet = SideBet())
