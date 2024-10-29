package actors.ab

//Standard Packages

import actors.MainActor._
import akka.actor.{Actor, ActorRef, Props, Stash, Timers}
import model.ab.data._
import model.ab.{ABJsonCodecs, Card, Deck52}
import model.common.data.{AdminClientData, GameCard}
import play.api.Logger
import play.api.libs.json.Json
import services.{AndarBaharSSeaterTableService, GameService}

import java.text.SimpleDateFormat
import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps


object AndarBaharTableActor {
  val name = "andarbahar-table-actor"
  val path = s"/usr/$name"


  //Self Triggered Messages
  case object TimerKey

  case object PollTick

  case object Init


  case class AdminConnected(ip: String, actor: ActorRef, client: ActorRef)

  case class AdminDisConnected(ip: String)

  case class TopperConnected(ip: String, actor: ActorRef, client: ActorRef)

  case class TopperDisConnected(ip: String)

  case class PlayerConnected(ip: String, actor: ActorRef, client: ActorRef)

  case class PlayerDisConnected(ip: String)

  case class PlayerBetPlaced(ip: String, betsList: BetsList, client: ActorRef)


  case object ShuffleDeck
  case object AutoPlayToggleCommand
  case class InfoPaperShow(show: Boolean)
  case class ConfigUpdateCommand(configData: ConfigData)
  case class WinResultMsgCommand(winResult: GameResult, cardCount: Int = 1 )


  case class CardDrawn(card: Card)


  def props(tableId: String,
            gameName: String,
            gameService: GameService,
            tableService: AndarBaharSSeaterTableService,
            logManagerActor: ActorRef,
            mainActor: ActorRef,
           ): Props = Props(new AndarBaharTableActor(tableId, gameName, gameService, tableService, logManagerActor, mainActor))


}

class AndarBaharTableActor(tableId: String = "228000",
                           gameName: String,
                           gameService: GameService,
                           tableService: AndarBaharSSeaterTableService,
                           logManagerActor: ActorRef,
                           mainActor: ActorRef,
                          )
  extends TykheTable(tableId, gameName, gameService, tableService, logManagerActor, mainActor)
    with Actor
    with Stash
    with Timers
    with ABJsonCodecs {

  import AndarBaharTableActor._


  private val deck52 = Deck52


  //This method is called when the actor is first created, and on all restarts.
  override def preStart(): Unit = {

    log.info(s"AndarBahar Table Actor Pre Start...")
    super.preStart()

    timers.startSingleTimer(TimerKey, Init, timeout = 1 second)

  }

  override def postStop(): Unit = {
    log.info("AndarBahar Table Actor Stopped...")
    super.postStop()
  }


  override def receive: Receive = TABLE_STATE_0_CLOSED(state = TableState())


  /** *******************************************************************************************************
    *
    *
    *
    *
    *
    *
    */


  /** ******************************** TABLE_STATE_0_CLOSED   ********************************************* */
  def TABLE_STATE_0_CLOSED(state: TableState): Receive = {
    case Init =>
      log.info("Init Received")
      timers.startTimerWithFixedDelay(TimerKey, PollTick, 1 second)
      context become TABLE_STATE_1_SHUFFLE(state.copy(gameStatus = "SHUFFLE"))
    case PollTick =>
    case message =>
      log.info(s"Stashing $message because I can't handle it in the CLOSED state")

  }

  /** ******************************** TABLE_STATE_1_SHUFFLE   ********************************************* */
  def TABLE_STATE_1_SHUFFLE(state: TableState): Receive = {
    //From clients    
    case AdminConnected(ip, actor, client) => context become TABLE_STATE_1_SHUFFLE(state = handleAdminConnected(state, adminIp = ip, actor, client))
    case AdminDisConnected(ip) => context become TABLE_STATE_1_SHUFFLE(state = handleAdminDisconnected(state, adminIp = ip))
    case TopperConnected(ip, actor, client) => context become TABLE_STATE_1_SHUFFLE(state = handleTopperConnected(state, topperIp = ip, actor, client))
    case TopperDisConnected(ip) => context become TABLE_STATE_1_SHUFFLE(state = handleTopperDisconnected(state, topperIp = ip))
    case PlayerConnected(ip, actor, client) => context become TABLE_STATE_1_SHUFFLE(state = handlePlayerConnected(state, playerIp = ip, actor, client,self))
    case PlayerDisConnected(ip) => context become TABLE_STATE_1_SHUFFLE(state = handlePlayerDisconnected(state, playerIp = ip))

    //From dealer
    case ShuffleDeck =>
    case AutoPlayToggleCommand => context become TABLE_STATE_1_SHUFFLE(state = handleAutoPlayToggle(state))
    case InfoPaperShow(show) => context become TABLE_STATE_1_SHUFFLE(state = handleInfoPaperShow(state, show))
    case ConfigUpdateCommand(configData) => context become TABLE_STATE_1_SHUFFLE(state = handleConfigUpdate(state, configData))
    case WinResultMsgCommand(winResult, cardCount) => context become TABLE_STATE_4_GAME_RESULT(state = handleGameResult(state, winResult.WinningHand, cardCount))

    //From main actor
    case PlayerBalanceUpdated(uid, balance) => context become TABLE_STATE_1_SHUFFLE(state = handleBalanceUpdate(state, uid, balance))
    case GuestConnectAccepted(ip, actor, client) => context become TABLE_STATE_1_SHUFFLE(state = handleGuestConnectAccepted(state, playerIp = ip, actor, client))
    
    //From outside
    case CardDrawn(card) => context become TABLE_STATE_2_PYB(state = handleJokerDraw(state, card) )
    
    case PollTick =>
      if (state.configData.autoPlay) {
        context become TABLE_STATE_1_SHUFFLE(state = state.copy(autoPlayCounter = state.autoPlayCounter + 1))
        if (state.autoPlayCounter == 7) {
          deck52.reShuffle()

          val drawnCard: Card = deck52.drawCard
          val updatedState = handleJokerDraw(state, drawnCard)
          context become TABLE_STATE_2_PYB(state = updatedState)
        }
      }

    case msg => log.error(s"message $msg not handled in TABLE_STATE_1_SHUFFLE state")
  }

  /** ******************************** TABLE_STATE_2_PYB   ********************************************* */

  def TABLE_STATE_2_PYB(state: TableState): Receive = {
    //From clients
    case AdminConnected(ip, actor, client) => context become TABLE_STATE_2_PYB(state = handleAdminConnected(state, adminIp = ip, actor, client))
    case AdminDisConnected(ip) => context become TABLE_STATE_2_PYB(state = handleAdminDisconnected(state, adminIp = ip))
    case TopperConnected(ip, actor, client) => context become TABLE_STATE_2_PYB(state = handleTopperConnected(state, topperIp = ip, actor, client))
    case TopperDisConnected(ip) => context become TABLE_STATE_2_PYB(state = handleTopperDisconnected(state, topperIp = ip))
    case PlayerConnected(ip, actor, client) => context become TABLE_STATE_2_PYB(state = handlePlayerConnected(state, playerIp = ip, actor, client, self))
    case PlayerDisConnected(ip) => context become TABLE_STATE_2_PYB(state = handlePlayerDisconnected(state, playerIp = ip))

    //From dealer
    case ShuffleDeck => context become TABLE_STATE_1_SHUFFLE(state = handleShuffleDeck(state))
    case AutoPlayToggleCommand => context become TABLE_STATE_1_SHUFFLE(state = handleAutoPlayToggle(state))
    case InfoPaperShow(show) => context become TABLE_STATE_2_PYB(state = handleInfoPaperShow(state, show))
    case ConfigUpdateCommand(_) =>
    case WinResultMsgCommand(winResult, cardCount) => context become TABLE_STATE_4_GAME_RESULT(state = handleGameResult(state, winResult.WinningHand, cardCount))


    //From main actor  
    case GuestConnectAccepted(ip, actor, client) => context become TABLE_STATE_2_PYB(state = handleGuestConnectAccepted(state, playerIp = ip, actor, client))
    case PlayerBalanceUpdated(uid, balance) => context become TABLE_STATE_2_PYB(state = handleBalanceUpdate(state, uid, balance))

    //From outside
    case CardDrawn(_) =>
    
    case PollTick =>
        context become TABLE_STATE_2_PYB(state = state.copy(autoPlayCounter = state.autoPlayCounter + 1))
        if (state.autoPlayCounter == 10) {
          val updatedState = handlePYB2DrawingCards(state)
          context become TABLE_STATE_3_NO_MORE_BETS(state = updatedState)
        }
    case msg => log.error(s"message $msg not handled in TABLE_STATE_2_PYB state")
  }

  /** ******************************** TABLE_STATE_3_NO_MORE_BETS   ********************************************* */

  def TABLE_STATE_3_NO_MORE_BETS(state: TableState): Receive = {
    //From clients
    case AdminConnected(ip, actor, client) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleAdminConnected(state, adminIp = ip, actor, client))
    case AdminDisConnected(ip) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleAdminDisconnected(state, adminIp = ip))
    case TopperConnected(ip, actor, client) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleTopperConnected(state, topperIp = ip, actor, client))
    case TopperDisConnected(ip) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleTopperDisconnected(state, topperIp = ip))
    case PlayerConnected(ip, actor, client) => context become TABLE_STATE_3_NO_MORE_BETS(state = handlePlayerConnected(state, playerIp = ip, actor, client, self))
    case PlayerDisConnected(ip) => context become TABLE_STATE_3_NO_MORE_BETS(state = handlePlayerDisconnected(state, playerIp = ip))
    case PlayerBetPlaced(ip, betsList, client) =>
      context become TABLE_STATE_3_NO_MORE_BETS(state = handlePlayerBetPlaced(state, playerIp = ip, betsList, client))    


    //From dealer  
    case ShuffleDeck => context become TABLE_STATE_1_SHUFFLE(state = handleShuffleDeck(state))
    case AutoPlayToggleCommand => context become TABLE_STATE_1_SHUFFLE(state = handleAutoPlayToggle(state))
    case InfoPaperShow(show) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleInfoPaperShow(state, show))
    case ConfigUpdateCommand(_) =>
    case WinResultMsgCommand(winResult, cardCount) => context become TABLE_STATE_4_GAME_RESULT(state = handleGameResult(state, winResult.WinningHand, cardCount))


    //From mainActor  
    case GuestConnectAccepted(ip, actor, client) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleGuestConnectAccepted(state, playerIp = ip, actor, client))
    case PlayerBalanceUpdated(uid, balance) => context become TABLE_STATE_3_NO_MORE_BETS(state = handleBalanceUpdate(state, uid, balance))
    
    //From outside
    case CardDrawn(card) =>
      val drawnCard = card
      log.info(s"Card Drawn is ${drawnCard.toString} for ${state.turn}")
      state.turn match {
        case (rnd: Int, player: Int) =>  //Initial Draw For Player & Banker
          val nextPlayer = if (player == 2) 1 else 2
          val nextRound = if (player == 2) rnd + 1 else rnd

          val updatedState = handlePlayerCardsDrawn(
            state,
            turn = (nextRound, nextPlayer),
            drawnCard,
            player
          )

          if (state.JokerCard(0) == Card.parseGameCard(drawnCard.toString).CardName(0)) {
            val WinningHand = if (player == 1) "Bahar" else "Andar"
            context become TABLE_STATE_4_GAME_RESULT(state = handleGameResult(updatedState, WinningHand, (rnd - 1) * 2 + player))
          } else {
            context become TABLE_STATE_3_NO_MORE_BETS(state = updatedState)
          }
        }

    case PollTick =>
      if (state.configData.autoPlay) {
        context become TABLE_STATE_3_NO_MORE_BETS(state = state.copy(autoPlayCounter = state.autoPlayCounter + 1))
        if (state.autoPlayCounter == 2) {

          val drawnCard = deck52.drawCard
          log.info(s"Card Drawn is ${drawnCard.toString} for ${state.turn}")
          state.turn match {
            case (rnd: Int, player: Int) =>  //Initial Draw For Player & Banker
              val nextPlayer = if (player == 2) 1 else 2
              val nextRound = if (player == 2) rnd + 1 else rnd

              val updatedState = handlePlayerCardsDrawn(
                state,
                turn = (nextRound, nextPlayer),
                drawnCard,
                player
              )

              if (state.JokerCard(0) == Card.parseGameCard(drawnCard.toString).CardName(0)) {
                val WinningHand = if (player == 1) "Bahar" else "Andar"
                context become TABLE_STATE_4_GAME_RESULT(state = handleGameResult(updatedState, WinningHand, (rnd - 1) * 2 + player))
              } else {
                context become TABLE_STATE_3_NO_MORE_BETS(state = updatedState)
              }

          }
        }
      }
    case msg => log.error(s"message $msg not handled in TABLE_STATE_3_NO_MORE_BETS state")
  }

  /** ******************************** TABLE_STATE_4_GAME_RESULT   ********************************************* */


  def TABLE_STATE_4_GAME_RESULT(state: TableState): Receive = {
    //From Clients    
    case AdminConnected(ip, actor, client) => context become TABLE_STATE_4_GAME_RESULT(state = handleAdminConnected(state, adminIp = ip, actor, client))
    case AdminDisConnected(ip) => context become TABLE_STATE_4_GAME_RESULT(state = handleAdminDisconnected(state, adminIp = ip))
    case TopperConnected(ip, actor, client) => context become TABLE_STATE_4_GAME_RESULT(state = handleTopperConnected(state, topperIp = ip, actor, client))
    case TopperDisConnected(ip) => context become TABLE_STATE_4_GAME_RESULT(state = handleTopperDisconnected(state, topperIp = ip))
    case PlayerConnected(ip, actor, client) => context become TABLE_STATE_4_GAME_RESULT(state = handlePlayerConnected(state, playerIp = ip, actor, client, self))
    case PlayerDisConnected(ip) => context become TABLE_STATE_4_GAME_RESULT(state = handlePlayerDisconnected(state, playerIp = ip))

    //From Dealer
    case InfoPaperShow(show) => context become TABLE_STATE_4_GAME_RESULT(state = handleInfoPaperShow(state, show))
    case AutoPlayToggleCommand => context become TABLE_STATE_1_SHUFFLE(state = handleAutoPlayToggle(state))
    case ShuffleDeck =>
    case ConfigUpdateCommand(_) =>

    //From Main Actor  
    case GuestConnectAccepted(ip, actor, client) => context become TABLE_STATE_4_GAME_RESULT(state = handleGuestConnectAccepted(state, playerIp = ip, actor, client))
    case PlayerBalanceUpdated(uid, balance) => context become TABLE_STATE_4_GAME_RESULT(state = handleBalanceUpdate(state, uid, balance))

    //From outside
    case CardDrawn(_) =>
      
    case PollTick =>
        context become TABLE_STATE_4_GAME_RESULT(state = state.copy(autoPlayCounter = state.autoPlayCounter + 1))
        if (state.autoPlayCounter == 5) {
          val updatedState = handleGameResult2Shuffle(state)
          context become TABLE_STATE_1_SHUFFLE(state = updatedState)
        }
    case msg => log.error(s"message $msg not handled in TABLE_STATE_4_GAME_RESULT state")
  }

  /*
                                                                                                            *
                                                                                                            *
                                                                                                            *
                                                                                                            *
                                                                                                            *
                                                                                                            *
  ***********************************************************************************************************/


  def handleGameResult2Shuffle(tableState: TableState): TableState = {
    //Go to new state
    val updatedState = tableState.copy(
      roundId = tableState.roundId + 1,
      gameStatus = "SHUFFLE",
      autoPlayCounter = 0,
      JokerCard = "SHUFFLE",
      BaharCards = Seq.empty[GameCard],
      AndarCards = Seq.empty[GameCard],
      drawCardCounter = 0,
      players = tableState.players.map { client => client._1 -> client._2.copy(betsList = BetsList(), wonBetsList = BetsList()) },
      turn = (1, 1)
    )

    //send shuffle message
    updatedState.sendShuffleDeckMsgToClients()
    //Reset Other Data


    updatedState
  }

  def handleGameResult(tableState: TableState, WinningHand: String, cardCount: Int): TableState = {

    val gameResult = GameResult(
      roundId = tableState.roundId,
      JokerCard = tableState.JokerCard,
      BaharCards = tableState.BaharCards,
      AndarCards = tableState.AndarCards,
      cardCount = cardCount,
      WinningHand = WinningHand,
    )

    val resultDetails = Json.toJson(gameResult).toString()

    //Go to new state
    val updatedState = tableState.copy(
      gameStatus = "GAME_RESULT",
      autoPlayCounter = 0,
      WinningHand = WinningHand,
      History = tableState.History :+ gameResult
    )

    val players = tableState.players

    /*
    * Go through each player client
    * if total bet value is > 0
    *   if total win value is > 0
    *     - update player balance in state
    *     - update player balance in accounts
    *     - trigger a game transaction - "Win"
    *     - game result msg with win
    *     - current balance msg with updated amount
    *   else
    *    - trigger a game transaction - "NoWin"
    *     - game result msg with win=0
    * else
    *   - game result msg with win=0
    *
    *
    * */

    tableState.players.foreach {
      client =>
        val betsList = client._2.betsList
        val totalBetsValue = getTotalBetsValue(betsList = betsList)
        val betDetails = Json.toJson(betsList).toString()

        if(totalBetsValue > 0) {
          /* Get the wonBetsList */
          val wonBetsList = getWonBetsList(betsList = betsList, gameResult = gameResult, cardCount )
          val totalWonBetsValue = getTotalWonBetsValue(wonBetsList = wonBetsList)

          if (totalWonBetsValue > 0) {
            //player won some amount
            val oldBalance = client._2.balance //1 Read Current Balance
            val newBalance = oldBalance +  totalWonBetsValue
            val details = Json.toJson(wonBetsList).toString()


            mainActor ! TablePlayerBetWon(
                tableId = tableId,
                gameName = gameName,
                roundId = tableState.roundId,
                details = details,
                betDetails = betDetails,
                resultDetails = resultDetails,
                playerIp = client._1,
                amount = totalWonBetsValue,
                tableActorRef = self
              )


            //player data updated
            players(client._1) = client._2.copy(balance = newBalance, wonBetsList = wonBetsList)

            //send game result
            updatedState.sendGameResultMsg(client._2.client, winAmount = totalWonBetsValue)
            //send new balance
            updatedState.sendCurrentBalanceMsg(client._2.client, uid = client._1, balance = newBalance )
          }
          else {
            val details = Json.toJson(wonBetsList).toString()

            //player lost some amount so, a game transaction
            mainActor ! TablePlayerBetLost(
                tableId = tableId,
                gameName = gameName,
                roundId = tableState.roundId,
                details = details,
                betDetails = betDetails,
                resultDetails = resultDetails,
                playerIp = client._1,
                amount = 0,
                tableActorRef = self
              )

            //send game result
            updatedState.sendGameResultMsg(client._2.client)
          }

        }
        else {
          //send game result
          updatedState.sendGameResultMsg(client._2.client)
        }
    }

    //send game result
    updatedState.sendGameResultMsgToClients()

    val playersBetDetails     = players.map(x => x._2.uid -> x._2.betsList).toList
    val playersWonBetsDetails = players.map(x => x._2.uid -> x._2.wonBetsList).toList
    val playersTotalBet       =  players.map(x => x._2.uid -> getTotalBetsValue(betsList = x._2.betsList)).toList
    val playersTotalWin       =  players.map(x => x._2.uid -> getTotalWonBetsValue(wonBetsList = x._2.wonBetsList)).toList

    //Send a Round Result to Main/Game Actor
    mainActor ! TableRoundResultIndication(
      tableId = tableId,
      gameName = gameName,
      roundId = tableState.roundId,
      winningHand = WinningHand,
      playersTotalBet = playersTotalBet,
      playersTotalWin = playersTotalWin,
      gameResult = Json.prettyPrint(Json.toJson(gameResult)),
      playerBetsList = Json.prettyPrint(Json.toJson(playersBetDetails)),
      playerWonBetsList = Json.prettyPrint(Json.toJson(playersWonBetsDetails))
    )


    updatedState.copy(players = players)
  }

  def handlePlayerCardsDrawn(tableState: TableState, turn: (Int, Int), drawnCard: Card, player: Int): TableState = {

    //first send Card message to clients
    tableState.sendCardMsgToClients(
      if (player == 1) "Bahar" else "Andar",
      0,
      Card.parseGameCard(drawnCard.toString).CardName,
      Card.parseGameCard(drawnCard.toString).CardValue,
    )

    val baharCards = if (player == 1) tableState.BaharCards.toList ::: List(Card.parseGameCard(drawnCard.toString)) else tableState.BaharCards
    val andarCards = if (player == 2) tableState.AndarCards.toList ::: List(Card.parseGameCard(drawnCard.toString)) else tableState.AndarCards

    //Go to new state
    val updatedState = tableState.copy(
      autoPlayCounter = 0,
      turn = turn,
      AndarCards = andarCards,
      BaharCards = baharCards
    )
    updatedState
  }

  def handlePYB2DrawingCards(tableState: TableState): TableState = {

    //Go to new state
    val updatedState = tableState.copy(
      gameStatus = "NO_MORE_BETS",
      autoPlayCounter = 0,
      turn = (1, 1))

    //send no more bets
    updatedState.sendNoMoreBetsMsgToClients()
    updatedState
  }

  def handleJokerDraw(tableState: TableState, drawnCard: Card): TableState = {

    //first send Card message to clients
    tableState.sendCardMsgToClients(
      "Joker",
      cardHandValue = 0,
      cardName = Card.parseGameCard(drawnCard.toString).CardName,
      cardValue = Card.parseGameCard(drawnCard.toString).CardValue,
    )


    //Go to new state
    val updatedState = tableState.copy(
      gameStatus = "PLACE_YOUR_BETS",
      autoPlayCounter = 0,
      JokerCard = Card.parseGameCard(drawnCard.toString).CardName
    )
    //send place your bets
    updatedState.sendPlaceYourBetsMsgToClients()
    updatedState
  }

  def handlePlayerBetPlaced(tableState: TableState, playerIp: String, betsList: BetsList, client: ActorRef): TableState = {
    /**
      * Logic Steps
      *  1 Read Current Balance
      *  2 Compute totalBetValue
      *  3 player account service update
      *  4 send PLAYER_UPDATED msg to admins - PLAYER_UPDATED
      *  5 create Money transaction record
      *  6 send PLAYER_BET_PLACED msg to all admins -PLAYER_BET_PLACED
      *  7 Update table state (balance, betList)
      *  8 send a current Balance msg to client - CURRENT_BALANCE
      */
    val players = tableState.players
    if(players.contains(playerIp)) {
      val player = players(playerIp)
      val oldBalance = players(playerIp).balance//1 Read Current Balance
      val totalBetValue = getTotalBetsValue(betsList)//2 Compute totalBetValue
      val newBalance = oldBalance - totalBetValue
      val details = Json.toJson(betsList).toString()

      mainActor ! TablePlayerBetPlaced(
          tableId = tableId,
          gameName = gameName,
          roundId = tableState.roundId,
          details = details,
          playerIp = playerIp,
          amount = totalBetValue,
          tableActorRef = self
        )

      //7 Update table state (balance, betList)
      players(playerIp) = player.copy(balance = newBalance, betsList = betsList )
      val updatedTableState = tableState.copy(players = players)
      //8 send a current Balance msg to client - CURRENT_BALANCE
      updatedTableState.sendCurrentBalanceMsg(client, player.uid, players(playerIp).balance)


      updatedTableState
    } else {
      tableState
    }
  }

  def handleBalanceUpdate(tableState: TableState, uid: String, newBalance: Double): TableState = {
    tableState.players.find(p => p._2.uid == uid) match {
      case Some(player) =>
        val players = tableState.players
        val client = player._2.client
        val playerIp = player._1
        val clientData = player._2

        players(playerIp) = clientData.copy(balance = newBalance)
        val updatedTableState = tableState.copy(players = players)
        updatedTableState.sendCurrentBalanceMsg(client, uid, newBalance)
        updatedTableState
      case None =>
        tableState
    }
  }


  /*
   *
   *
   *
   *
   *
***********************************************************************************************************/


} //end of class AndarBaharTableActor


trait AndarBaharTableUtilities {

  def getTotalBetsValue(betsList: BetsList): Double = {
    val mainBetsValue = {
      import betsList.{Andar2ndBet, AndarBet, Bahar2ndBet, BaharBet}

       AndarBet + BaharBet + Andar2ndBet + Bahar2ndBet
    }
    val sideBetsValue = {
      import betsList.SideBets._
      Joker_1_5 + Joker_6_10 + Joker_11_15 + Joker_16_25 + Joker_26_30 + Joker_31_35 + Joker_36_40 + Joker_41
    }

    mainBetsValue + sideBetsValue
  }


  def getWonBetsList(betsList: BetsList, gameResult: GameResult, cardCount: Int): BetsList = {
    import betsList.SideBets._
    import betsList.{AndarBet, BaharBet}

    BetsList(
      AndarBet = if (gameResult.WinningHand == "Andar") AndarBet * 2 else 0,
      BaharBet = if (gameResult.WinningHand == "Bahar") BaharBet * 1.9 else  0,
      SideBets = SideBet(
        Joker_1_5  = if ((cardCount >= 1 ) && (cardCount <= 5)) Joker_1_5 * 3.5 else 0,
        Joker_6_10 = if ((cardCount >= 6 ) && (cardCount <= 10)) Joker_6_10 * 4.5 else 0,
        Joker_11_15 = if ((cardCount >= 11 ) && (cardCount <= 15)) Joker_11_15 * 5.5 else 0,
        Joker_16_25 = if ((cardCount >= 16 ) && (cardCount <= 25)) Joker_16_25 * 4.5 else 0,
        Joker_26_30 = if ((cardCount >= 26 ) && (cardCount <= 30)) Joker_26_30 * 15 else 0,
        Joker_31_35 = if ((cardCount >= 31 ) && (cardCount <= 35)) Joker_31_35 * 25 else 0,
        Joker_36_40 = if ((cardCount >= 36 ) && (cardCount <= 40)) Joker_36_40 * 50 else 0,
        Joker_41 = if (cardCount >= 41  ) Joker_41 * 120 else 0,
      )
    )
  }

  def getTotalWonBetsValue(wonBetsList: BetsList): Double = {
    val mainBetsValue = {
      import wonBetsList.{Andar2ndBet, AndarBet, Bahar2ndBet, BaharBet}
      AndarBet + BaharBet + Andar2ndBet + Bahar2ndBet
    }
    val sideBetsValue = {
      import wonBetsList.SideBets._
      Joker_1_5 + Joker_6_10 + Joker_11_15 + Joker_16_25 + Joker_26_30 + Joker_31_35 + Joker_36_40 + Joker_41
    }

    mainBetsValue + sideBetsValue
  }
}

/*
* When to Use Traits?
* - If it might be reused in multiple, unrelated classes, make it a trait
* - Traits allow code reuse
* - Traits cannot receive parameters
* */
trait commonTableUtilities {


}

/*
* When to Use Abstarct Class?
* - A single Abstract class can be inherited by a class
* - Abstract class can receive parameters
* */

abstract class TykheTable(tableId: String,
                          gameName: String,
                          gameService: GameService,
                          tableService: AndarBaharSSeaterTableService,
                          logManagerActor: ActorRef,
                          mainActor: ActorRef)
extends AndarBaharTableUtilities
with commonTableUtilities
{

  /* Licence Setup Changes */

//  import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
//
//  private var productValid = true;
//  private var licenseValid = false;
//  private var licenseFormatValid = false;
//
//  val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd")
//  private var profitCodes = MMap(
//    "4260441857" -> ("2023-01-01", "2023-02-05"),
//    "9645318040" -> ("2023-02-01", "2023-03-05"),
//    "9645618040" -> ("2023-01-01", "2023-12-01"),
//  )
//
//  private var productCodes = MMap(
//    "2509330773" -> "96:11:5A:6C:9F:28"
//  )

  /* License Setup End */
  val log: Logger = Logger(this.getClass)

  def handleAdminConnected(tableState: TableState,
                           adminIp: String,
                           actor: ActorRef,
                           client: ActorRef): TableState = {


    log.info("handleAdminConnected is called")

    var admins = tableState.admins

    if (admins.contains(adminIp)) {
      admins(adminIp) = admins(adminIp).copy(actor = actor, client = client)
      log.info("Admin reconnected")
    } else {
      admins = admins ++ Map(adminIp -> AdminClientData(actor = actor, client = client))
      log.info("Admin Fresh Connection")
    }

    val updatedState = tableState.copy(admins = admins)
    updatedState.sendInitialDataAdminMsg(clientData = admins(adminIp))
    updatedState.sendInitialConfigMsg(admins(adminIp).client)

    updatedState

  }


  def handleTopperConnected(tableState: TableState,
                            topperIp: String,
                            actor: ActorRef,
                            client: ActorRef): TableState = {

    log.info("handleTopperConnected is called")
    var toppers = tableState.toppers

    if (toppers.contains(topperIp)) {
      toppers(topperIp) = toppers(topperIp).copy(actor = actor, client = client)
      log.info("Topper reconnected")
    } else {
      toppers = toppers ++ Map(topperIp -> ClientData(actor = actor, client = client))
      log.info("Topper Fresh Connection")

    }

    val updatedState = tableState.copy(toppers = toppers)

    tableState.sendInitialDataMsg(clientData = toppers(topperIp), destination = "topper")
    tableState.sendInitialConfigMsg(toppers(topperIp).client)
    tableState.gameStatus match {
      case "PLACE_YOUR_BETS" =>
        tableState.sendPlaceYourBetsMsg(client = client)
      case "NO_MORE_BETS" =>
        tableState.sendNoMoreBetsMsg(client = client)
      case "GAME_RESULT" =>
        tableState.sendGameResultMsg(client = client)
      case "SHUFFLE" =>
        tableState.sendShuffleDeckMsg(client = client)
      case _ =>
    }
    updatedState

  }



  def handleGuestConnectAccepted(tableState: TableState,
                            playerIp: String,
                            actor: ActorRef,
                            client: ActorRef): TableState = {

    var players = tableState.players
    //Now check again
    gameService.getPlayers.find(_.clientIp == playerIp) match {
      case Some(player) =>
        //update table state - players
        players = players ++ Map(playerIp -> ClientData(uid = player.uid, balance = player.balance, actor = actor, client = client))
        val updatedState = tableState.copy(players = players)

        tableState.sendInitialDataMsg(clientData = players(playerIp), destination = "player")
        tableState.sendInitialConfigMsg(players(playerIp).client)
        tableState.sendCurrentBalanceMsg(players(playerIp).client, players(playerIp).uid, players(playerIp).balance)
        tableState.gameStatus match {
          case "PLACE_YOUR_BETS" =>
            tableState.sendPlaceYourBetsMsg(client = client)
          case "NO_MORE_BETS" =>
            tableState.sendNoMoreBetsMsg(client = client)
          case "GAME_RESULT" =>
            tableState.sendGameResultMsg(client = client)
          case "SHUFFLE" =>
            tableState.sendShuffleDeckMsg(client = client)
          case _ =>
        }

        mainActor ! PlayerStatusOnline(playerIp)

        updatedState
      case None =>
        tableState
    }
  }


  def handlePlayerConnected(tableState: TableState,
                            playerIp: String,
                            actor: ActorRef,
                            client: ActorRef, tableActorRef: ActorRef): TableState = {
    /**
      * Logic Steps - For handling a player connection request
      * if the player IP is unknown
      *   1 if the player is in players accounts data
    */
    var players = tableState.players

    if (players.contains(playerIp)) {
      players -= playerIp
    } else {
      log.info(s"A Player with IP $playerIp Trying to connect...")
      val playerOpt = gameService.getPlayers.find(_.clientIp == playerIp)
      playerOpt match {
        case Some(foundPlayer) =>
          //if the player is in players accounts data
          log.info(s"An existing IP Connected...$playerIp => uid=${foundPlayer.uid} balance=${foundPlayer.balance}")
        case None =>
          log.info(s"Guest IP $playerIp Trying to connect..")
          mainActor ! GuestConnectRequest(playerIp, actor, client, tableActorRef)
      }

    }

    gameService.getPlayers.find(_.clientIp == playerIp) match {
      case Some(player) =>
        //update table state - players
        players = players ++ Map(playerIp -> ClientData(uid = player.uid, balance = player.balance, actor = actor, client = client))
        val updatedState = tableState.copy(players = players)

        tableState.sendInitialDataMsg(clientData = players(playerIp), destination = "player")
        tableState.sendInitialConfigMsg(players(playerIp).client)
        tableState.sendCurrentBalanceMsg(players(playerIp).client, players(playerIp).uid, players(playerIp).balance)
        tableState.gameStatus match {
          case "PLACE_YOUR_BETS" =>
            tableState.sendPlaceYourBetsMsg(client = client)
          case "NO_MORE_BETS" =>
            tableState.sendNoMoreBetsMsg(client = client)
          case "GAME_RESULT" =>
            tableState.sendGameResultMsg(client = client)
          case "SHUFFLE" =>
            tableState.sendShuffleDeckMsg(client = client)
          case _ =>
        }

        mainActor ! PlayerStatusOnline(playerIp)

        updatedState
      case None =>
        tableState
    }




  }


  def handleAdminDisconnected(tableState: TableState, adminIp: String): TableState = {
    val admins = tableState.admins
    if (admins.contains(adminIp)) admins -= adminIp
    val updatedState = tableState.copy(admins = admins)
    updatedState
  }

  def handleTopperDisconnected(tableState: TableState, topperIp: String): TableState = {
    val toppers = tableState.toppers
    if (toppers.contains(topperIp)) toppers -= topperIp
    val updatedState = tableState.copy(toppers = toppers)
    updatedState
  }


  def handlePlayerDisconnected(tableState: TableState, playerIp: String): TableState = {
    val players = tableState.players
    if (players.contains(playerIp)) {
      players -= playerIp
      //Turn player status to offline by
      mainActor ! PlayerStatusOffline(playerIp)
    }
    val updatedState = tableState.copy(players = players)

    updatedState
  }


  def handleInfoPaperShow(tableState: TableState, show: Boolean): TableState = {
    val updatedState = tableState.copy(configData = tableState.configData.copy(showInfoPaper = show))
    updatedState.sendConfigUpdateMsgToClients()
    updatedState
  }

  def handleConfigUpdate(tableState: TableState, configData: ConfigData): TableState = {
    val updatedConfigData = tableState.configData.copy(
      tableLimit = configData.tableLimit,
      tableName = configData.tableName,
      tableDifferential = configData.tableDifferential,
    )
    val updatedState = tableState.copy(configData = updatedConfigData)
    updatedState.sendConfigUpdateMsgToClients()
    updatedState
  }

  def handleAutoPlayToggle(tableState: TableState): TableState = {
    log.info("Auto Play Toggle Received")

    val updatedConfigData = tableState.configData.copy(autoPlay = !tableState.configData.autoPlay)

    val updatedState = tableState.copy(
      configData = updatedConfigData,
      roundId = tableState.roundId,
      gameStatus = "SHUFFLE",
      autoPlayCounter = 0,
      drawCardCounter = 0,
      JokerCard = "SHUFFLE",
      BaharCards = Seq.empty[GameCard],
      AndarCards = Seq.empty[GameCard],
      players = tableState.players.map { client => client._1 -> client._2.copy(betsList = BetsList(), wonBetsList = BetsList()) },
      turn = (1, 1)
    )
    updatedState.sendConfigUpdateMsgToClients()
    updatedState.sendShuffleDeckMsgToClients()
    updatedState
  }

  def handleShuffleDeck(tableState: TableState): TableState = {
    log.info("Shuffle Deck received")
    //Go to new state
    val updatedState = tableState.copy(
      roundId = tableState.roundId,
      gameStatus = "SHUFFLE",
      autoPlayCounter = 0,
      drawCardCounter = 0,
      JokerCard = "SHUFFLE",
      BaharCards = Seq.empty[GameCard],
      AndarCards = Seq.empty[GameCard],
      players = tableState.players.map { client => client._1 -> client._2.copy(betsList = BetsList(), wonBetsList = BetsList()) },
      turn = (1, 1)
    )

    //send shuffle message
    updatedState.sendShuffleDeckMsgToClients()
    //Reset Other Data

    updatedState
  }

}

