package services


import akka.actor.{ActorRef, ActorSystem}
import dao.{AndarBaharDao, PlayerDao}

import actors.ab.AndarBaharTableActor
import model.common.data.{LicenseData, Player}

class AndarBaharSSeaterTableService(actorSystem: ActorSystem,
                                    gameService: GameService,
                                    playerDao: PlayerDao,
                                    andarbaharDao: AndarBaharDao)
{

  var actorAndarBaharTable: ActorRef = _

  def init(): Unit = {
    actorAndarBaharTable = actorSystem.actorOf(AndarBaharTableActor.props(
      tableId = "228000",
      gameName = "AndarBahar",
      gameService = gameService,
      tableService = this,
      gameService.getLoggingActor,
      mainActor = gameService.getMainActor
    ), AndarBaharTableActor.name)

    gameService.tableServiceAdded(tableId = "228000", actorAndarBaharTable)
  }

  def getAndarBaharTableActor: ActorRef = actorAndarBaharTable


  def getInitialDataJsonString: String = andarbaharDao.getInitialDataJsonString

  def authenticateJsonString: String = andarbaharDao.authenticateJsonString

  def sendStreamsJsonString: String = andarbaharDao.sendStreamsJsonString
  
  def getLicenseData: LicenseData = andarbaharDao.getLicenseData()


}
