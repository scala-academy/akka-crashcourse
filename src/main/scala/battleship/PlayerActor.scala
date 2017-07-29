package battleship

import akka.actor.{Actor, Props}
import battleship.PlayerActor.BoatPlacementResult
import battleship.game.{Boat, BoatLocation, ShotResult}

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

  override def receive: Receive = {
    case _ => sender() ! BoatPlacementResult(Set.empty)
  }
}
