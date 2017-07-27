package battleship.game

import scala.util.Random

/**
  * Created by jordidevos on 27/07/2017.
  */
class StupidRandomPlayer(seed: Int = Random.nextInt) extends Player {
  val rnd = new Random(seed)

  override def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatPlacement)] = {

    def canPlace(boatToPlace: Boat, potentialPlacement: BoatPlacement, acc: Set[(Boat, BoatPlacement)]): Boolean = {
      def coordinatesTaken(x: Int, y: Int) =
        acc.exists { case (boat, placement) => boat.liesOn(placement, x, y) }

      val coordinatesPotentialPlacement = boatToPlace.coordinates(potentialPlacement)
      !coordinatesPotentialPlacement.exists { case (x, y) => coordinatesTaken(x, y) }
    }

    def findBoatPlacement(boat: Boat, acc: Set[(Boat, BoatPlacement)]): BoatPlacement = {
      val newPlacement =
        if (rnd.nextBoolean()) BoatPlacement(rnd.nextInt(boardSize - boat.size), rnd.nextInt(boardSize), true)
        else BoatPlacement(rnd.nextInt(boardSize), rnd.nextInt(boardSize - boat.size), false)

      if (canPlace(boat, newPlacement, acc)) newPlacement
      else findBoatPlacement(boat, acc)
    }

    def placeBoatsR(todo: Seq[Boat], acc: Set[(Boat, BoatPlacement)]): Set[(Boat, BoatPlacement)] = todo match {
      case Nil =>
        acc
      case boat +: bs =>
        val newPlacement: (Boat, BoatPlacement) = (boat, findBoatPlacement(boat, acc))
        placeBoatsR(bs, acc + newPlacement)
    }

    placeBoatsR(boats, Set.empty)
  }


  override def getNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)]): (Int, Int) = {
    val location = (rnd.nextInt(boardSize), rnd.nextInt(boardSize))
    if (shotHistory.exists(_._1 == location)) {
      getNextShot(boardSize, shotHistory)
    } else {
      location
    }
  }

  override def name: String = s"RandomPlayer($seed)"
}
