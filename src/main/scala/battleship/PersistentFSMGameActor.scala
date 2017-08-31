package battleship

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._
import akka.persistence._
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import battleship.PersistentFSMGameActor.{GameState, Uninitialised, _}

import scala.reflect.ClassTag

/**
  * Created by M06F947 on 30-8-2017.
  */

object PersistentFSMGameActor {

  def props: Props = Props(new PersistentFSMGameActor)

  sealed trait GameState extends FSMState {
    override def identifier: String = this.getClass.getName
  }

  case object Uninitialised extends GameState

  case object WaitingForBoatPlacement extends GameState

  case object RunningGame extends GameState

  case object GameEnded extends GameState

  case class PlayerState(boardState: BoardState, actorRef: ActorRef)

  //Todo: ActorRef isn't safe. Create test to show that it isn't, and create solution

  case class GameData(boardSize: Int, currentPlayer: Int, playersStates: Map[Int, PlayerState], actorRefMap: Map[ActorRef, Int])


  sealed trait GameEvent

  case class InitialiseEvent(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat]) extends GameEvent

  case class BoatSetupEvent(sender: Int, placement: Set[(Boat, BoatLocation)]) extends GameEvent

  case class MoveEvent(x: Int, y: Int) extends GameEvent

  case class GameEndedEvent(winner: ActorRef) extends GameEvent

  sealed trait GameCommand

  case class InitGame(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat]) extends GameCommand

  case class BoatSetup(placement: Set[(Boat, BoatLocation)]) extends GameCommand

  case class Move(x: Int, y: Int) extends GameCommand

  case object GameStateRequest extends GameCommand

}

class PersistentFSMGameActor(implicit val domainEventClassTag: ClassTag[GameEvent]) extends PersistentFSM[GameState, GameData, GameEvent] {
  //Dont know why I have to use the domaineventclasstag

  override def persistenceId = context.self.path.toString

  startWith(Uninitialised, GameData(0, 0, Map(),Map()))

  def placeBoats(placements: Set[(Boat, BoatLocation)]): BoardState = {
    placements.foldLeft(BoardState.empty) {
      case (boardState, (boat, location)) => boardState.placeBoat(boat, location)
    }
  }

  def setupBoats(preGameData: GameData, sender: Int, placement: Set[(Boat, BoatLocation)]): GameData = {
    val players = preGameData.playersStates
    preGameData.copy(playersStates = preGameData.playersStates + (1 - sender -> PlayerState(placeBoats(placement), players(1 - sender).actorRef)))
  }

  override def applyEvent(event: GameEvent, dataPreEvent: GameData): GameData = {
    event match {
      case InitialiseEvent(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat]) =>
        GameData(boardSize, 0, Map(0 -> PlayerState(BoardState.empty, player1), 1 -> PlayerState(BoardState.empty, player2)), Map(player1 -> 0, player2 -> 1))
      case BoatSetupEvent(sender: Int, placement: Set[(Boat, BoatLocation)]) => setupBoats(dataPreEvent, sender, placement)
      case MoveEvent(x: Int, y: Int) =>
        val cp: Int = dataPreEvent.currentPlayer
        val ps = dataPreEvent.playersStates
        dataPreEvent.copy(currentPlayer = 1 - cp, playersStates = Map(1 - cp -> ps(1 - cp), cp -> ps(cp).copy(boardState = ps(cp).boardState.processShot(x, y))))
    }
  }

  when(Uninitialised) {
    case Event(InitGame(boardSize, player1, player2, boatSet), _) =>
      println("Initgame received")
      player1 ! PlaceBoats(boatSet, boardSize)
      player2 ! PlaceBoats(boatSet, boardSize)
      goto(WaitingForBoatPlacement) applying InitialiseEvent(boardSize, player1, player2, boatSet)
  }

  when(WaitingForBoatPlacement) {
    case Event(BoatSetup(placement: Set[(Boat, BoatLocation)]), gameData) => {
      val copyGameData = setupBoats(gameData, gameData.actorRefMap(sender()), placement)
      if ((copyGameData.playersStates(0).boardState == BoardState.empty) || (copyGameData.playersStates(1).boardState == BoardState.empty))
        stay applying BoatSetupEvent(gameData.actorRefMap(sender()), placement)
      else
        goto(RunningGame) applying BoatSetupEvent(gameData.actorRefMap(sender()), placement)
    }
  }

  when(RunningGame) {
    case Event(Move(x, y), gameData) => {
      val cp: Int = gameData.currentPlayer
      val ps = gameData.playersStates
      val copyGameData = gameData.copy(playersStates = Map(1 - cp -> ps(1 - cp), cp -> ps(cp).copy(boardState = ps(cp).boardState.processShot(x, y))))
      if (copyGameData.playersStates(gameData.currentPlayer).boardState.allShipsSunk)
        goto(GameEnded) applying MoveEvent(x,y)
      else
        stay applying MoveEvent(x,y)

    }
  }

}
