package model.ab.data

case class ConfigData(tableLimit: TableBetLimit = TableBetLimit(),
                      tableName: String = "EMPTY",
                      tableDifferential: Int = 0,
                      showInfoPaper: Boolean = false,
                      autoDraw: Boolean = false,
                      autoPlay: Boolean = false,
                      isOppositeBettingAllowed: Boolean = true)
