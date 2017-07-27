package battleship

/**
  * Created by jordidevos on 27/07/2017.
  */
package object game {

  case class Boat(size: Int) {
    def coordinates(placement: BoatPlacement): Seq[(Int, Int)] =
      if (placement.isHorizontal) (0 to size).map(i => (placement.x, placement.y + i))
      else (0 to size).map(i => (placement.x + i, placement.y))

    def liesOn(placement: BoatPlacement, x: Int, y: Int): Boolean = coordinates(placement).contains((x, y))
  }

  /**
    * Coordinates start at 0
    */
  type Coordinate = (Int, Int)

  sealed trait ShotResult

  case class Sink(boat: Boat) extends ShotResult

  case object Miss extends ShotResult

  case object Hit extends ShotResult

  type Shot = (Coordinate, ShotResult)

  case class BoatPlacement(x: Int, y: Int, isHorizontal: Boolean)

  trait Player {
    def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatPlacement)]

    def getNextShot(boardSize: Int, shotHistory: Seq[Shot]): Coordinate

    def name: String

    override def toString: String = s"Player $name"
  }

}
