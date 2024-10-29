package dao

import os.Path
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import java.text.SimpleDateFormat
import java.util.Calendar
import model.common.CommonJsonCodecs
import model.common.data.Player
import model.common.messages._

class PlayerDao extends CommonJsonCodecs {

  val log: Logger = Logger(this.getClass)
  val dateFormat =  new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss.SSS z")
  
  val fileNamePlayers: Path = os.home / "lobby" /  "users.json"
  val fileNameOperations: Path = os.home / "lobby" /  "operations.json"


  if (!os.exists(fileNamePlayers)) {
    os.write.over(
      fileNamePlayers,
      Json.prettyPrint(Json.toJson(Seq.empty[Player])),
      createFolders = true
    )
    log.logger.info(s"Backup Players Missing... Created Success..")
  }


  if (!os.exists(fileNameOperations)) {
    os.write.over(
      fileNameOperations,
      Json.prettyPrint(Json.toJson(Seq.empty[OperationTransactionMsg])),
      createFolders = true
    )
    log.logger.info(s"Backup Operations Missing... Created Success..")
  }

  val playersString: String = os.read(fileNamePlayers);
  val playersJson: JsValue = Json.parse(playersString);
  var players: Seq[Player] = playersJson.as[Seq[Player]]

  val operationsString: String = os.read(fileNameOperations);
  val operationsJson: JsValue = Json.parse(operationsString);
  var operations: Seq[OperationTransactionMsg] = operationsJson.as[Seq[OperationTransactionMsg]]

  def getPlayersData: Seq[Player] = players
  def getOperationTransactionsData(): Seq[OperationTransactionMsg] = operations

  def getPlayerData(player: String): Player = {
    players.find(p => p.clientIp == player).getOrElse(
      Player(
        uid = (players.size + 1).toString, //TODO now I am filling ip as uid
        clientIp = player,
        balance = 0,
        nickname = "Guest"
      )
    )
  }

  def addPlayer(player: Player): Seq[Player] = {
    players = players.+:(player)

    os.write.over(
      fileNamePlayers,
      Json.prettyPrint(Json.toJson(players)),
      createFolders = true
    )

    players
  }
  def updatePlayer(player: Player, uid: String): Unit = {
    val foundIndex = players.indexWhere(p => p.uid == uid)
    if(foundIndex != -1) {
      players = players.updated(foundIndex, player)
    } else {
      log.logger.warn("Player not found in updatePlayer! Added a new Player")
      players = players.+:(player);
    }

    os.write.over(
      fileNamePlayers,
      Json.prettyPrint(Json.toJson(players)),
      createFolders = true
    )
  }

  def addOperationTransaction(operation: OperationTransactionMsg): Unit = {
    operations = operations.+:(operation)
    os.write.over(
      fileNameOperations,
      Json.prettyPrint(Json.toJson(operations))
    )

  }


}
