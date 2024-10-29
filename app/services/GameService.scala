package services


import actors.LogManagerActor.AddLog
import actors.MainActor.TableServiceAdded
import actors._
import akka.actor.{ActorRef, ActorSystem}
import dao.{LobbyDao, LogDao, PlayerDao}
import play.api.Logger
import model.common.messages._
import model.common.data.{LicenseData, Player}

class GameService(actorSystem: ActorSystem,
                  playerDao: PlayerDao,
                  lobbyDao: LobbyDao,
                  logDao: LogDao) {

  val log: Logger = Logger(this.getClass)

  var actorMain: ActorRef = _
  val actorLogging: ActorRef = actorSystem.actorOf(LogManagerActor.props(logDao))

  def init(): Unit = {
    actorLogging ! AddLog(content =  s"Actor System is : ${actorSystem.name}");
    actorLogging ! AddLog(content =  s"Actor System Uptime : ${actorSystem.uptime}");
    actorMain = actorSystem.actorOf(MainActor.props(gameService = this), MainActor.name)

  }

  def getMainActor: ActorRef = actorMain
  def getLoggingActor: ActorRef = actorLogging

  //PLAYER RELATED


  def getPlayers: Seq[Player] = playerDao.getPlayersData


  def getPlayerData(player: String): Player = {
    val playerData = playerDao.getPlayerData(player)
    if (playerData.nickname == "Guest") {
      val updatedPlayerData = playerData.copy(nickname = s"Guest${player}");
      playerDao.addPlayer(updatedPlayerData)
      updatedPlayerData
    } else playerData
  }

  def updatePlayerData(player: Player, uid: String): Unit = playerDao.updatePlayer(player, uid)


  def getOperationTransactions: Seq[OperationTransactionMsg] = playerDao.getOperationTransactionsData()

  def addOperationTransaction(transaction: OperationTransactionMsg): Unit = playerDao.addOperationTransaction(transaction)


  //LOBBY RELATED

  def getMoneyTransactions: Seq[MoneyTransactionMsg] = lobbyDao.getMoneyTransactionsData()

  def getGameTransactions: Seq[GameTransactionMsg] = lobbyDao.getGameTransactionsData()

  def getRoundTransactions: Seq[RoundTransactionMsg] = lobbyDao.getRoundTransactionsData()


  def addTransaction(transaction: MoneyTransactionMsg): Unit = lobbyDao.addMoneyTransaction(transaction)

  def addGameTransaction(transaction: GameTransactionMsg): Unit = lobbyDao.addGameTransaction(transaction)

  def addRoundTransaction(transaction: RoundTransactionMsg): Unit = lobbyDao.addRoundTransaction(transaction)



  def tableServiceAdded(tableId: String, tableActorRef: ActorRef) = {
    actorMain ! TableServiceAdded(tableId, tableActorRef);
  }

}
