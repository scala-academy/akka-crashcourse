package battleship.game

/**
  * Created by jordidevos on 28/07/2017.
  */
case class Boat(size: Int) {

  def coordinates(location: BoatLocation): Seq[(Int, Int)] =
    if (location.isHorizontal) (0 until size).map(i => (location.x + i, location.y))
    else (0 until size).map(i => (location.x, location.y + i))

  def liesOn(location: BoatLocation, x: Int, y: Int): Boolean = coordinates(location).contains((x, y))
}
