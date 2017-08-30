package battleship

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import battleship.GameActor._
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._

/**
  * Created by m06f947 on 3-8-2017.
  */






object GameActor {
  //Incoming
  case class StartGame(boardSize: Int, player1: ActorRef, player2: ActorRef, boatSet: Seq[Boat])

  case class BoatSetup(placement: Set[(Boat, BoatLocation)])

  case class Move(x: Int, y: Int)

  case object GameStateRequest

  //Outgoing



  type BoardStates = Map[ActorRef, BoardState]



  sealed trait GameState

  case object Uninitialised extends GameState
  case object WaitingForBoatPlacement extends GameState
  case class GameStarted(boardStates: BoardStates) extends GameState
  case class GameEnded(winner: ActorRef) extends GameState



  def props: Props = Props(new GameActor)

}

class GameActor extends Actor with ActorLogging {

  def placeBoats(placements: Set[(Boat, BoatLocation)]): BoardState = {
    placements.foldLeft(BoardState.empty) {
      case (boardState, (boat, location)) => boardState.placeBoat(boat, location)
    }
  }

  def swapCurrentPlayer(one: ActorRef, two: ActorRef, current: ActorRef) = {
    if (one == current) two
    else one
  }

  override def receive: Receive = {
    case StartGame(size, player1, player2, boatSet) => {
      player1 ! PlaceBoats(boatSet, size)
      player2 ! PlaceBoats(boatSet, size)
      context.become(gameInit(size, player1, player2))
    }
    case GameStateRequest => sender() ! Uninitialised
  }

  def gameInit(size: Int, player1: ActorRef, player2: ActorRef, boardStates: BoardStates=Map.empty): Receive = {
    case BoatSetup(placement) if (sender() == player1 || sender() == player2) =>
      val nextBoardStates = boardStates + (sender() -> placeBoats(placement))
      if (nextBoardStates.size == 2) {
        player2 ! GetNextShot(size, nextBoardStates(player2).history)
        context.become(gameStarted(player2, size, player1, player2, player2, nextBoardStates))
      } else {
        context.become(gameInit(size, player1, player2, nextBoardStates))
      }
    case GameStateRequest => sender() ! WaitingForBoatPlacement
  }

  def gameStarted(gameStarter: ActorRef, size: Int, player1: ActorRef, player2: ActorRef, currentPlayer: ActorRef, boardStates: BoardStates): Receive = {
    case Move(x, y) if sender() == currentPlayer =>
      val nextBoardStates = boardStates + (currentPlayer -> boardStates(currentPlayer).processShot((x, y)))
      if (nextBoardStates(currentPlayer).allShipsSunk) {
        player1 ! GameEnded(currentPlayer)
        player2 ! GameEnded(currentPlayer)
        log.info("Player " + currentPlayer + " has won!!")
        context.become(endgame(currentPlayer, boardStates))
      }
      else {
        val currentPlayer2 = swapCurrentPlayer(player1, player2, currentPlayer)
        currentPlayer2 ! GetNextShot(size, nextBoardStates(currentPlayer2).history)
        context.become(gameStarted(gameStarter, size, player1, player2, currentPlayer2, nextBoardStates))
      }
    case GameStateRequest => sender() ! GameStarted(boardStates)
  }

  def endgame(currentPlayer: ActorRef, boardStates: Map[ActorRef, BoardState]): Receive = {
    case GameStateRequest => sender() ! GameEnded(currentPlayer)
    case _ => sender() ! GameEnded(currentPlayer)
  }
}

