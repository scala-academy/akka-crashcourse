package battleship.game

/**
  * Created by jordidevos on 26/07/2017.
  */

object BoardState {
  def empty: BoardState = BoardState(Set.empty, Seq.empty)
}

case class BoardState(boats: Set[(Boat, BoatPlacement)], history: Seq[Shot]) {

  def placeBoat(boat: Boat, boatPlacement: BoatPlacement): BoardState = {
    val placement = (boat, boatPlacement)
    BoardState(boats + placement, history)
  }

  def shoot(coordinate: Coordinate): BoardState = {
    require(!history.exists(_._1 == coordinate), "Can't shoot at the same coordinate twice")

    val shotResult = calculateShotResult(coordinate)
    BoardState(boats, (coordinate, shotResult) +: history)
  }

  def gameOver: Boolean = boats.size == history.count(_._2 match {
    case Sink(_) => true
    case _ => false
  })

  def calculateShotResult(coordinate: Coordinate): ShotResult = {
    def shotsOnBoatSoFar(boat: Boat, placement: BoatPlacement) =
      history.count { case ((x, y), _) => boat.coordinates(placement).contains((x, y)) }

    val boatShotAt = boats.find { case (boat, placement) =>
      boat.liesOn(placement, coordinate._1, coordinate._2)
    }

    boatShotAt match {
      case None => Miss
      case Some((boatBeingHit, placement)) =>
        if (shotsOnBoatSoFar(boatBeingHit, placement) < boatBeingHit.size) Hit
        else Sink(boatBeingHit)
    }
  }
}
