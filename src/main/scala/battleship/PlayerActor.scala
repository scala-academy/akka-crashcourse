package battleship

import akka.actor.{Actor, Props}
import battleship.PlayerActor.{BoatPlacementResult, GetNextShot, NextShot, PlaceBoats}
import battleship.game._

/**
  * Created by jordidevos on 28/07/2017.
  */
object PlayerActor {
  case class PlaceBoats(boats: Seq[Boat], boardSize: Int)
  case class BoatPlacementResult(placement: Set[(Boat, BoatLocation)])
  case class GetNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)])
  case class NextShot(x: Int, y: Int)
  def linearPlayerProps: Props = Props(new LinearPlayerActor)
  def stupidRandomPlayerProps: Props = Props(new StupidRandomPlayerActor)
}

class PlayerActor extends Actor {
//def placeBoats(boats: Seq[Boat], boardSize:Int) = //
  player: Player =>

  override def receive: Receive = {
    case PlaceBoats(boats, boardSize) => sender() ! BoatPlacementResult(player.placeBoats(boats,boardSize))
    case GetNextShot(boardSize,shotHistory) =>
      val nextShot = player.getNextShot(boardSize,shotHistory)
      sender() ! NextShot(nextShot._1,nextShot._2)
  }
}

class LinearPlayerActor extends PlayerActor with LinearPlayer
class StupidRandomPlayerActor extends PlayerActor with StupidRandomPlayer
