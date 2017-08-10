package battleship

import akka.actor.{Actor, Props}
import battleship.PlayerActor.{BoatPlacementResult, GetNextShot, NextShot, PlaceBoats}
import battleship.game.{Boat, BoatLocation, LinearPlayer, ShotResult}

/**
  * Created by jordidevos on 28/07/2017.
  */
object PlayerActor {
  case class PlaceBoats(boats: Seq[Boat], boardSize: Int)

  case class BoatPlacementResult(placement: Set[(Boat, BoatLocation)])

  case class GetNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)])

  case class NextShot(x: Int, y: Int)

  def props: Props = Props(new PlayerActor)
}

class PlayerActor extends Actor {

  //Uses LinearPlayer as base

  override def receive: Receive = {
    case PlaceBoats(boats, boardSize) => sender() ! BoatPlacementResult(LinearPlayer.placeBoats(boats,boardSize))
    case GetNextShot(boardSize,shotHistory) => {
      val nextShot = LinearPlayer.getNextShot(boardSize,shotHistory)
      sender() ! NextShot(nextShot._1,nextShot._2)
    }
  }
}
