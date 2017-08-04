package battleship

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import battleship.GameActor._
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._

/**
  * Created by m06f947 on 3-8-2017.
  */



object GameActor {

  case class StartGame(boardsize:Int, player1:ActorRef, player2:ActorRef, boatset: Seq[Boat])

  //case class WhatIsBoatSetup(boats:Seq[Boat], boardsize: Int)
  case class ThisIsBoatSetup(placement: Set[(Boat,BoatLocation)])
  //case class WhatIsNextMove(boardsize:Int, shotHistory: Seq[((Int,Int),ShotResult)])
  case class ThisIsNextMove(x: Int, y: Int)


  def props: Props = Props(new GameActor)

}

class GameActor extends Actor with ActorLogging {


  //internal state
  var boardsize = Game.defaultBoardSize
  var boatSet = Game.defaultBoatSet
  //first of pair is currentplayer
  var players: (ActorRef, ActorRef) = (Actor.noSender, Actor.noSender)
  def currentPlayer = players._1
  def swapPlayers = {players = (players._2, players._1)}
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

  def numberofboatsetupsreceived:Int = {boatsetup.count((a: (ActorRef, Set[(Boat, BoatLocation)])) => true)}

  //*****************************************************************************************************************
  //This is not functional programming at all, these functions are there only for the side effects...****************

  def initGame(size:Int,player1:ActorRef,player2:ActorRef,boatset:Seq[Boat]):Any = {
    players = (player1, player2)
    boardsize = size
    boatSet = boatset
    players._1 ! PlaceBoats(boatset, boardsize)
    sender() ! "Game started!"
  }


  def addboatsetup(placement:Set[(Boat,BoatLocation)]):Any = {
    boatsetup += (currentPlayer -> placement)
  }
  def setupplayerfields():Any ={
    boardstates += (players._1 -> initPlayerState(players._2))
    boardstates += (players._2 -> initPlayerState(players._1))
  }
  def updateaftermove(x:Int,y:Int):Any = boardstates += (currentPlayer -> boardstates(currentPlayer).processShot((x, y)))

  //This is not functional programming at all, these functions are there only for the side effects...****************
  //*****************************************************************************************************************
    /*
    def setupgame
    def firstboatsetup
    def secondboatsetup
    def processshot
     */



  override def receive: Receive = {
    case StartGame(size, player1, player2, boatset) => {
      initGame(size, player1, player2, boatset)
    }
    case ThisIsBoatSetup(placement) if (sender() == currentPlayer) && (numberofboatsetupsreceived < 1) => {
        addboatsetup(placement)
        swapPlayers
        currentPlayer ! PlaceBoats(boatSet, boardsize)
    }
    case ThisIsBoatSetup(placement) if (sender() == currentPlayer) && (numberofboatsetupsreceived < 2) => {
        addboatsetup(placement)
        setupplayerfields()
        currentPlayer ! GetNextShot(boardsize, List())
    }
    case ThisIsNextMove(x, y) if sender() == currentPlayer => {
      updateaftermove(x,y)
      if (boardstates(currentPlayer).allShipsSunk) {
        currentPlayer ! ("Player " + currentPlayer +  " has won!!") //this is for test only
        log.info("Player " + currentPlayer +  " has won!!")
      }
      else {
        swapPlayers
        currentPlayer ! GetNextShot(boardsize, List())
      }
    }
  }
}

