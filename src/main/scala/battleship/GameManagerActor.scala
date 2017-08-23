package battleship

import akka.actor.{Actor,ActorRef,Props}
import battleship.GameActor.{StartGame,props}
import battleship.GameManagerActor._
import battleship.game.Boat

/**
  * Created by jordidevos on 28/07/2017.
  *
  * A Game manager manages (multiple) games between two players
  * The game is fixed to single board size and boat set after the manager is started
  *
  */
object GameManagerActor {

  case class StartManager(size: Int,boatSet: Seq[Boat])

  case class CreateGame(player1: ActorRef,player2: ActorRef)

  /**
    * Returned when a new game is created. The returned id should be unique for this game
    *
    * @param id
    */
  case class GameCreated(id: Int)

  case class PlayGame(id: Int)

  /**
    * Returned when a game is finished
    */
  case class GameEnded(winner: String)

  def props: Props = Props(new GameManagerActor)


  // Private State management
  private trait GameManagerState {
    def size: Int
    def boats: Seq[Boat]
    def createGame(game: ActorRef,p1: ActorRef,p2: ActorRef): (Int,GameManagerState)
    def gameStarted(starter: ActorRef,id: Int): GameManagerState
    def getGameWithPlayers(id: Int): GameWithPlayers
    def getStarter(gameActor: ActorRef): ActorRef
  }

  private case class GameWithPlayers(game: ActorRef,player1: ActorRef,player2: ActorRef)

  private case class GameManagerStateImpl(size: Int,
                                          boats: Seq[Boat],
                                          games: Map[Int,GameWithPlayers],
                                          gamesToStarters: Map[ActorRef,ActorRef]) extends GameManagerState {
    def createGame(game: ActorRef,p1: ActorRef,p2: ActorRef): (Int, GameManagerStateImpl) = {
      val gameID = games.size
      (gameID,copy(games = this.games + (gameID -> GameWithPlayers(game,p1,p2))))
    }
    def gameStarted(starter: ActorRef,id: Int): GameManagerState = {
      copy(gamesToStarters = this.gamesToStarters + (games(id).game -> starter))
    }
    def getGameWithPlayers(id: Int): GameWithPlayers = games(id)
    def getStarter(gameActor: ActorRef): ActorRef = gamesToStarters(gameActor)
  }

  private def createGameManagerState(size: Int,boats: Seq[Boat]): GameManagerState = {
    GameManagerStateImpl(size,boats,Map(),Map())
  }
}

/**
  * The following trait is used to inject the logic of creating game actors into the GameManagerActor. This allows other
  * logic to be injected for testing purposes
  */
trait GameActorCreator {
  def createGameActor: ActorRef
}

class GameManagerActor extends Actor with GameActorCreator {

  override def receive: Receive = {
    case StartManager(size: Int,boatSet: Seq[Boat]) =>
      context.become(managerStarted(createGameManagerState(size,boatSet)))
  }
  def managerStarted(state: GameManagerState): Receive = {
    case CreateGame(p1,p2) =>
      val (id,nstate) = state.createGame(createGameActor,p1,p2)
      sender() ! GameCreated(id)
      context.become(managerStarted(nstate))
    case PlayGame(gameID) =>
      val gwp = state.getGameWithPlayers(gameID)
      gwp.game ! StartGame(state.size,gwp.player1,gwp.player2,state.boats)
      context.become(managerStarted(state.gameStarted(sender(),gameID)))
    case GameActor.GameEnded(winner) =>
      state.getStarter(sender()) ! GameManagerActor.GameEnded(winner.toString())
  }

  override def createGameActor: ActorRef =
    context.actorOf(GameActor.props)
}
