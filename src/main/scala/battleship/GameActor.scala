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

  case class ThisIsBoatSetup(placement: Set[(Boat,BoatLocation)])

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

  def swapCurrentPlayer(one:ActorRef,two:ActorRef,current:ActorRef) = {
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

  def gameinit1(size:Int,player1:ActorRef,player2:ActorRef,boatset: Seq[Boat]): Receive = {
    case ThisIsBoatSetup(placement1) if (sender() == player1) =>
      player2 ! PlaceBoats(boatset, size)
      context.become(gameinit2(size, player1, player2, boatset,placement1))
    case WhatIsGameState => sender() ! GameNotStartedYet(1)
  }

  def gameinit2(size:Int,player1:ActorRef,player2:ActorRef,boatset1:Seq[Boat],placement1:Set[(Boat,BoatLocation)]): Receive = {
    case ThisIsBoatSetup(placement2) if (sender() == player2) =>
      val boardstates = Map(player1 -> placeBoats(placement2),player2 -> placeBoats(placement1))
      player2 ! GetNextShot(size, boardstates(player2).history)
      context.become(gamestarted(size,player1,player2,player2,boardstates))
    case WhatIsGameState => sender() ! GameNotStartedYet(2)
  }

  def gamestarted(size:Int,player1:ActorRef,player2:ActorRef,currentplayer:ActorRef,boardstates: Map[ActorRef, BoardState] ):Receive = {
    case ThisIsNextMove(x, y) if sender() == currentplayer =>
      val nextboardstates = boardstates + (currentplayer -> boardstates(currentplayer).processShot((x, y)))
      if (nextboardstates(currentplayer).allShipsSunk) {
        currentplayer ! ("Player " + currentplayer +  " has won!!") //this is for test only
        log.info("Player " + currentplayer +  " has won!!")
        context.become(endgame(currentplayer,boardstates))
      }
      else {
        val currentPlayer2 = swapCurrentPlayer(player1,player2,currentplayer)
        currentPlayer2 ! GetNextShot(size, nextboardstates(currentPlayer2).history)
        context.become(gamestarted(size,player1,player2,currentPlayer2,nextboardstates))
      }
    case WhatIsGameState => sender() ! boardstates
  }
  def endgame(currentplayer: ActorRef, boardstates: Map[ActorRef, BoardState]):Receive = {
    case WhatIsGameState => sender() ! boardstates
    case _ => sender() ! "The game ended and " + currentplayer +  " has won"
  }




/*
old definition

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


  def numberofboatsetupsreceived:Int = {boatsetup.count((a: (ActorRef, Set[(Boat, BoatLocation)])) => true)}
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
  */


}

