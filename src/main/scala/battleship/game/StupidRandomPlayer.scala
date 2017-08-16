package battleship.game

import scala.language.implicitConversions
import scala.util.Random

/**
  * Created by jordidevos on 27/07/2017.
  */
/*
//TODO: wat?
object StupidRandomPlayer {

  /**
    * Demonstrate the usage of a type class: even though the class StupidRandomPlayer does not extend the Player trait,
    * the following type class instance makes it possible for instances of the class StupidRandomPlayer to be used where
    * an instance of the Player trait is required
    */
  implicit def randomPlayerInstance(srp: StupidRandomPlayer): Player = new Player {
    import srp._

    override def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatLocation)] = placeBoats(boats, boardSize)

    override def getNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)]): (Int, Int) = getNextShot(boardSize, shotHistory)

    override def name: String = s"RandomPlayer($seed)"
  }

}
*/

trait StupidRandomPlayer extends Player {

  val seed: Int = Random.nextInt
  val rnd = new Random(seed)


  override def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatLocation)] = {

    def canPlace(boatToPlace: Boat, potentialPlacement: BoatLocation, acc: Set[(Boat, BoatLocation)]): Boolean = {
      def coordinatesTaken(x: Int, y: Int) =
        acc.exists { case (boat, boatLocation) => boat.liesOn(boatLocation, x, y) }

      val coordinatesPotentialPlacement = boatToPlace.coordinates(potentialPlacement)
      !coordinatesPotentialPlacement.exists { case (x, y) => coordinatesTaken(x, y) }
    }

    def findBoatPlacement(boat: Boat, acc: Set[(Boat, BoatLocation)]): BoatLocation = {
      val potentialLocation =
        if (rnd.nextBoolean()) BoatLocation(rnd.nextInt(boardSize - boat.size), rnd.nextInt(boardSize), true)
        else BoatLocation(rnd.nextInt(boardSize), rnd.nextInt(boardSize - boat.size), false)

      if (canPlace(boat, potentialLocation, acc)) potentialLocation
      else findBoatPlacement(boat, acc)
    }

    def placeBoatsR(todo: Seq[Boat], acc: Set[(Boat, BoatLocation)]): Set[(Boat, BoatLocation)] = todo match {
      case Nil =>
        acc
      case boat +: bs =>
        val newPlacement: (Boat, BoatLocation) = (boat, findBoatPlacement(boat, acc))
        placeBoatsR(bs, acc + newPlacement)
    }

    placeBoatsR(boats, Set.empty)
  }

  override def getNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)]): (Int, Int) = {
    val location = (rnd.nextInt(boardSize), rnd.nextInt(boardSize))
    if (shotHistory.exists { case (shotCoordinates, _) => shotCoordinates == location }) {
      getNextShot(boardSize, shotHistory)
    } else {
      location
    }
  }

  override def name: String = "StupidRandomPlayer"
}
