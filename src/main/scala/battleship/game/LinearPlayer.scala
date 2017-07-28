package battleship.game

/**
  * Created by jordidevos on 27/07/2017.
  */
object LinearPlayer extends Player {

  override def getNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)]): (Int, Int) = {
    val shotNr = shotHistory.size
    val x = shotNr % boardSize
    val y = shotNr / boardSize
    (x, y)
  }

  override def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatLocation)] = {
    for (boat <- boats) require(boat.size <= boardSize, s"Cannot place $boat on board with size $boardSize")
    (for (i <- boats.indices) yield (boats(i), BoatLocation(0, i, isHorizontal = true))).toSet
  }

  override def name: String = "LinearPlayer"
}
