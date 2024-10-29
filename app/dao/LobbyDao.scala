package dao

import model.common.CommonJsonCodecs
import model.common.messages._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import java.text.SimpleDateFormat

class LobbyDao extends CommonJsonCodecs {

  val log: Logger = Logger(this.getClass)
  val dateFormat =  new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss.SSS z")
  

  val fileNameMoneyTransactions: os.Path = os.home / "lobby" /  "moneyTransactions.json"
  val fileNameGameTransactions: os.Path = os.home / "lobby" /  "gameTransactions.json"
  val fileNameRoundTransactions: os.Path = os.home / "lobby" /  "roundTransactions.json"



  if (!os.exists(fileNameMoneyTransactions)) {
    os.write.over(
      fileNameMoneyTransactions,
      Json.prettyPrint(Json.toJson(Seq.empty[MoneyTransactionMsg]
      )),
      createFolders = true
    )
    log.logger.info(s"Backup Transactions Missing... Created Success..")
  }

  if (!os.exists(fileNameGameTransactions)) {
    os.write.over(
      fileNameGameTransactions,
      Json.prettyPrint(Json.toJson(Seq.empty[GameTransactionMsg])),
      createFolders = true
    )
    log.logger.info(s"Backup Game Transactions Missing... Created Success..")
  }

  //What if file removed ??
  if(!os.exists(fileNameRoundTransactions)) {
    os.write.over(
      fileNameRoundTransactions,
      Json.prettyPrint(Json.toJson(Seq.empty[RoundTransactionMsg])),
      createFolders = true
    )
    log.logger.info("Backup Round Transactions Missing... Created Success")
  }

  val moneyTransactionsString: String = os.read(fileNameMoneyTransactions);
  val moneyTransactionsJson: JsValue = Json.parse(moneyTransactionsString);
  var moneyTransactions: Seq[MoneyTransactionMsg] = moneyTransactionsJson.as[Seq[MoneyTransactionMsg]]
  
  val gameTransactionsString: String = os.read(fileNameGameTransactions);
  val gameTransactionsJson: JsValue = Json.parse(gameTransactionsString);
  var gameTransactions: Seq[GameTransactionMsg] = gameTransactionsJson.as[Seq[GameTransactionMsg]]

  //read file String -> JsValue -> Seq[RoundTransactionMsg]
  val roundTransactionsString : String = os.read(fileNameRoundTransactions)
  val roundTransactionsJson: JsValue = Json.parse(roundTransactionsString)
  var roundTransactions: Seq[RoundTransactionMsg] = roundTransactionsJson.as[Seq[RoundTransactionMsg]]

  def getMoneyTransactionsData(): Seq[MoneyTransactionMsg] = moneyTransactions
  def getGameTransactionsData(): Seq[GameTransactionMsg] = gameTransactions
  //serve the records on get
  def getRoundTransactionsData(): Seq[RoundTransactionMsg] = roundTransactions

  def addMoneyTransaction(transaction: MoneyTransactionMsg): Unit = {
    moneyTransactions = moneyTransactions.+:(transaction)//prepend it
    os.write.over(
      fileNameMoneyTransactions,
      Json.prettyPrint(Json.toJson(moneyTransactions)),
      createFolders = true
    )
  }
  def addGameTransaction(gameTransaction: GameTransactionMsg): Unit = {
    gameTransactions = gameTransactions.+:(gameTransaction)//prepend it
    os.write.over(
      fileNameGameTransactions,
      Json.prettyPrint(Json.toJson(gameTransactions)),
      createFolders = true
    )
  }

  //add transaction to both Ram and Db
  def addRoundTransaction(roundTransaction: RoundTransactionMsg): Unit = {
    roundTransactions = roundTransactions.+:(roundTransaction)//prepend it
    os.write.over(
      fileNameRoundTransactions,
      Json.prettyPrint(Json.toJson(roundTransactions)),
      createFolders = true
    )
  }

}
