package battleship

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import battleship.GameActor._
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._

/**
  * Created by m06f947 on 3-8-2017.
  */


object GameActor {

  case class StartGame(boardsize: Int, player1: ActorRef, player2: ActorRef, boatset: Seq[Boat])

  case class ThisIsBoatSetup(placement: Set[(Boat, BoatLocation)])

  case class ThisIsNextMove(x: Int, y: Int)

  case object WhatIsGameState

  case class GameNotStartedYet(stage: Int)

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
    case StartGame(size, player1, player2, boatset) => {
      player1 ! PlaceBoats(boatset, size)
      sender() ! "Game started!"
      context.become(gameinit1(size, player1, player2, boatset))
    }
    case WhatIsGameState => sender() ! GameNotStartedYet(0)
  }

  def gameinit1(size: Int, player1: ActorRef, player2: ActorRef, boatset: Seq[Boat]): Receive = {
    case ThisIsBoatSetup(placement1) if sender() == player1 =>
      player2 ! PlaceBoats(boatset, size)
      context.become(gameinit2(sender(), size, player1, player2, placement1))
    case WhatIsGameState => sender() ! GameNotStartedYet(1)
  }

  def gameinit2(gamestarter: ActorRef, size: Int, player1: ActorRef, player2: ActorRef, placement1: Set[(Boat, BoatLocation)]): Receive = {
    case ThisIsBoatSetup(placement2) if sender() == player2 =>
      val boardstates = Map(player1 -> placeBoats(placement2), player2 -> placeBoats(placement1))
      player2 ! GetNextShot(size, boardstates(player2).history)
      context.become(gamestarted(gamestarter, size, player1, player2, player2, boardstates))
    case WhatIsGameState => sender() ! GameNotStartedYet(2)
  }

  def gamestarted(gamestarter: ActorRef,size: Int, player1: ActorRef, player2: ActorRef, currentplayer: ActorRef, boardstates: Map[ActorRef, BoardState]): Receive = {
    case ThisIsNextMove(x, y) if sender() == currentplayer =>
      val nextboardstates = boardstates + (currentplayer -> boardstates(currentplayer).processShot((x, y)))
      if (nextboardstates(currentplayer).allShipsSunk) {
        gamestarter ! "The game ended and " + currentplayer + " has won"
        currentplayer ! ("Player " + currentplayer + " has won!!") //this is for test only
        log.info("Player " + currentplayer + " has won!!")
        context.become(endgame(currentplayer, boardstates))
      }
      else {
        val currentPlayer2 = swapCurrentPlayer(player1, player2, currentplayer)
        currentPlayer2 ! GetNextShot(size, nextboardstates(currentPlayer2).history)
        context.become(gamestarted(gamestarter, size, player1, player2, currentPlayer2, nextboardstates))
      }
    case WhatIsGameState => sender() ! boardstates
  }

  def endgame(currentplayer: ActorRef, boardstates: Map[ActorRef, BoardState]): Receive = {
    case WhatIsGameState => sender() ! boardstates
    case _ => sender() ! "The game ended and " + currentplayer + " has won"
  }
}

