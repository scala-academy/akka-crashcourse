package battleship

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import battleship.GameActor._
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._
import akka.persistence._
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import battleship.PersistentFSMGameActor.{GameState => _, _}

/**
  * Created by M06F947 on 30-8-2017.
  */

object PersistentFSMGameActor {

  def props: Props = Props[PersistentFSMGameActor]

  sealed trait GameState extends FSMState {
    override def identifier: String = this.getClass.getName
  }
  case object Uninitialised extends GameState
  case object WaitingForBoatPlacement extends GameState
  case object GameStarted extends GameState
  case object GameEnded extends GameState

  case class PlayerState(boardState: BoardState, actorRef: ActorRef)

  case class GameData(boardSize: Int, players: Map[Int,PlayerState])

  sealed trait GameEvent
  case class GameStartedEvent(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat]) extends GameEvent
  case class BoatSetupEvent(sender: Int ,placement: Set[(Boat, BoatLocation)]) extends GameEvent
  case class MoveEvent(x: Int, y: Int) extends GameEvent


  sealed trait GameCommand
  case class StartGame(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat]) extends GameCommand
  case class BoatSetup(placement: Set[(Boat, BoatLocation)]) extends GameCommand
  case class Move(x: Int, y: Int) extends GameCommand
  case object GameStateRequest extends GameCommand

}

class PersistentFSMGameActor extends PersistentFSM[GameState,GameData,GameEvent] {


  override def applyEvent(event: GameEvent,dataPreEvent: GameData): GameData = {
    event match {
      case GameStartedEvent(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat]) =>
        GameData(boardSize,Map(0 -> PlayerState(BoardState.empty,player2),1 -> PlayerState(BoardState.empty,player1)))
      case BoatSetupEvent(placement: Set[(Boat, BoatLocation)]) =>
      case MoveEvent(x: Int, y: Int) =>
    }
  }
}

//player 1 -> boarstate, actorref
//player 2 -> boardstate, actorref