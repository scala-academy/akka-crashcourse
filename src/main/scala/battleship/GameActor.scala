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

  case class GameNotStartedYet(stage: GameState)

  type BoardStates = Map[ActorRef, BoardState]

  sealed trait GameState
  case object Uninitialised extends GameState
  case object Player1AskedForBoats extends GameState
  case object Player2AskedForBoats extends GameState
  case class GameStarted(boardStates: BoardStates) extends GameState
  case object GameEnded extends GameState

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
      sender() ! "Game started!"
      context.become(gameInit1(size, player1, player2, boatSet))
    }
    case GameStateRequest => sender() ! GameNotStartedYet(Uninitialised)
  }

  def gameInit1(size: Int, player1: ActorRef, player2: ActorRef, boatset: Seq[Boat]): Receive = {
    case BoatSetup(placement1) if sender() == player1 =>
      player2 ! PlaceBoats(boatset, size)
      context.become(gameInit2(sender(), size, player1, player2, placement1))
    case GameStateRequest => sender() ! GameNotStartedYet(Player1AskedForBoats)
  }

  def gameInit2(gameStarter: ActorRef, size: Int, player1: ActorRef, player2: ActorRef, placement1: Set[(Boat, BoatLocation)]): Receive = {
    case BoatSetup(placement2) if sender() == player2 =>
      val boardStates = Map(player1 -> placeBoats(placement2), player2 -> placeBoats(placement1))
      player2 ! GetNextShot(size, boardStates(player2).history)
      context.become(gameStarted(gameStarter, size, player1, player2, player2, boardStates))
    case GameStateRequest => sender() ! GameNotStartedYet(Player2AskedForBoats)
  }

  def gameStarted(gameStarter: ActorRef, size: Int, player1: ActorRef, player2: ActorRef, currentPlayer: ActorRef, boardStates: BoardStates): Receive = {
    case Move(x, y) if sender() == currentPlayer =>
      val nextBoardStates = boardStates + (currentPlayer -> boardStates(currentPlayer).processShot((x, y)))
      if (nextBoardStates(currentPlayer).allShipsSunk) {
        gameStarter ! "The game ended and " + currentPlayer + " has won"
        currentPlayer ! ("Player " + currentPlayer + " has won!!") //this is for test only
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
    case GameStateRequest => sender() ! boardStates
    case _ => sender() ! "The game ended and " + currentPlayer + " has won"
  }
}

