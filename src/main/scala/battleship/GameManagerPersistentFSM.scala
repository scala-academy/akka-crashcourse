package battleship

import akka.actor.{Actor, ActorPath, ActorRef}
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import battleship.GameManagerPersistentFSM._
import battleship.game.Boat

import scala.reflect.ClassTag

object GameManagerPersistentFSM {

  sealed trait Command

  case class StartManager(size: Int, boatSet: Seq[Boat]) extends Command

  case class CreateGame(player1: ActorRef, player2: ActorRef) extends Command

  case class StartGame(gameID: Int) extends Command

  sealed trait GameManagerState extends FSMState {
    def identifier = this.getClass.getCanonicalName
  }

  case object Uninitialized extends GameManagerState

  case object Started extends GameManagerState

  case class GameWithPlayers(game: ActorPath, player1: ActorPath, player2: ActorPath)

  sealed trait GameManagerData {
    def createGame(game: ActorPath, p1: ActorPath, p2: ActorPath): (Int, GameManagerData)

    def gameStarted(starter: ActorPath, id: Int): GameManagerData

    def getGameWithPlayers(id: Int): GameWithPlayers

    def getStarter(gameActor: ActorPath): ActorPath
  }

  case object NoData extends GameManagerData {
    def error = sys.error("no data")

    def createGame(game: ActorPath, p1: ActorPath, p2: ActorPath): (Int,GameManagerData) = (-1,this)

    def gameStarted(starter: ActorPath, id: Int): GameManagerData = this

    def getGameWithPlayers(id: Int): GameWithPlayers = null

    def getStarter(gameActor: ActorPath): ActorPath = null
  }

  case class GameManagerDataImpl(size: Int,
                                 boats: Seq[Boat],
                                 games: Map[Int, GameWithPlayers],
                                 gamesToStarters: Map[ActorPath, ActorPath]) extends GameManagerData {

    def createGame(game: ActorPath, p1: ActorPath, p2: ActorPath): (Int, GameManagerData) = {
      val gameID = games.size
      (gameID, copy(games = this.games + (gameID -> GameWithPlayers(game, p1, p2))))
    }

    def gameStarted(starter: ActorPath, id: Int): GameManagerData = {
      copy(gamesToStarters = this.gamesToStarters + (games(id).game -> starter))
    }

    def getGameWithPlayers(id: Int): GameWithPlayers = games(id)

    def getStarter(gameActor: ActorPath): ActorPath = gamesToStarters(gameActor)
  }

  def createGameManagerData(size: Int, boats: Seq[Boat]): GameManagerData = {
    GameManagerDataImpl(size, boats, Map(), Map())
  }

  sealed trait GameManagerEvent

  case class ManagerStarted(size: Int, boatSet: Seq[Boat]) extends GameManagerEvent

  case class GameCreated(player1: ActorPath, player2: ActorPath) extends GameManagerEvent

  case class GameStarted(starter: ActorPath, gameID: Int) extends GameManagerEvent

  trait GameActorCreator {
    def createGameActor: ActorRef
  }
}

class GameManagerPersistentFSM extends PersistentFSM[GameManagerState,GameManagerData,GameManagerEvent]
    with GameActorCreator {

  startWith(Uninitialized, NoData)

  // WHERE DO WE MANAGE IDs?
  lazy val persistenceId = "What to do"

  override def domainEventClassTag: ClassTag[GameManagerEvent] = ClassTag(classOf[GameManagerEvent])

  when(Uninitialized) {
    case Event(StartManager(size: Int, boatSet: Seq[Boat]),_) => goto(Started) applying ManagerStarted(size, boatSet)
  }

  when(Started) {
    case Event(CreateGame(player1,player2), _) => stay applying GameCreated(player1.path,player2.path)
    case Event(StartGame(gameID: Int), _) => stay applying GameStarted(sender().path,gameID)
  }

  override def applyEvent(event: GameManagerEvent, data: GameManagerData): GameManagerData = {
    event match {
      case ManagerStarted(size, boatSet) => createGameManagerData(size,boatSet)
      case GameCreated(player1,player2) => data.createGame(createGameActor.path,player1,player2)._2
      case GameStarted(starter: ActorPath, gameID: Int) => data.gameStarted(starter,gameID)
    }
  }

  override def createGameActor: ActorRef = context.actorOf(GameActor.props)
}

