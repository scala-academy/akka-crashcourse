package battleship

/**
  * Created by jordidevos on 27/07/2017.
  */
package object game {

  /**
    * Coordinates start at 0
    */
  type Coordinate = (Int, Int)

  sealed trait ShotResult

  case class Sink(boat: Boat) extends ShotResult

  case object Miss extends ShotResult

  case object Hit extends ShotResult

  type Shot = (Coordinate, ShotResult)

  case class BoatLocation(x: Int, y: Int, isHorizontal: Boolean)

  trait Player {
    def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatLocation)]

    def getNextShot(boardSize: Int, shotHistory: Seq[Shot]): Coordinate

    def name: String

    override def toString: String = s"Player $name"
  }

}
