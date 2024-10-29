package model.ab.data

case class StatisticsData(JokerStatistics: JokerStatisticsData = JokerStatisticsData(),
                          winPercentages: WinPercentages = WinPercentages(),
                          lastWinners : Seq[String] = Seq.empty[String],
                          lastJokers : Seq[String] = Seq.empty[String])
