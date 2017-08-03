package battleship.routes

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._
import battleship.routes.gameActor.{StartGame, ThisIsBoatSetup, ThisIsNextMove}

/**
  * Created by m06f947 on 3-8-2017.
  */



object gameActor {

  case class StartGame(boardsize:Int, player1:ActorRef, player2:ActorRef, boatset: Seq[Boat])

  //case class WhatIsBoatSetup(boats:Seq[Boat], boardsize: Int)
  case class ThisIsBoatSetup(placement: Set[(Boat,BoatLocation)])
  //case class WhatIsNextMove(boardsize:Int, shotHistory: Seq[((Int,Int),ShotResult)])
  case class ThisIsNextMove(x: Int, y: Int)


  def props: Props = Props(new gameActor)

}

class gameActor extends Actor with ActorLogging {


  //internal state
  var boardsize = 8
  var BoatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
  //first of pair is currentplayer
  var players: (ActorRef, ActorRef) = (Actor.noSender, Actor.noSender)
  //Have the boatsetup ready. In here player1 is boatsetup._1
  var boatsetup: Map[ActorRef, Set[(Boat, BoatLocation)]] = Map()
  //keep the state of the boards
  var boardstates: Map[ActorRef, BoardState] = Map()

  def initPlayerState(playerToPlaceBoats: ActorRef): BoardState = {
    val placements = boatsetup(playerToPlaceBoats)
    placements.foldLeft(BoardState.empty) {
      case (boardState, (boat, location)) => boardState.placeBoat(boat, location)
    }
  }

  override def receive: Receive = {
    case StartGame(size, player1, player2, boatset) => {
      players = (player1, player2)
      boardsize = size
      BoatSet = boatset
      players._1 ! PlaceBoats(boatset, boardsize)
      sender() ! "Game started!"
    }
    case ThisIsBoatSetup(placement) if sender() == players._1 => {
      //Right now: If players send multiple placements, they will overwrite the other.
      if (boatsetup.count((a: (ActorRef, Set[(Boat, BoatLocation)])) => true) < 1) {
        boatsetup += (players._1 -> placement)
        players = (players._2, players._1)
        players._1 ! PlaceBoats(BoatSet, boardsize)
      }
      else {
        boatsetup += (players._1 -> placement)
        boardstates += (players._1 -> initPlayerState(players._2))
        boardstates += (players._2 -> initPlayerState(players._1))
        players._1 ! GetNextShot(boardsize, List())
      }
    }
    case ThisIsNextMove(x, y) if sender() == players._1 => {
      boardstates += (players._1 -> boardstates(players._1).processShot((x, y)))
      if (boardstates(players._1).allShipsSunk) {
        players._1 ! ("Player " + players._1 +  " has won!!") //this is for test only
        log.info("Player " + players._1 +  " has won!!")
      }

      else {
        players = (players._2, players._1)
        players._1 ! GetNextShot(boardsize, List())
      }
    }
  }
}

