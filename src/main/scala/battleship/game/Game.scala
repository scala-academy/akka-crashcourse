package battleship.game

import java.util.concurrent.atomic.AtomicInteger

/**
  * Created by jordidevos on 26/07/2017.
  */
object Game {

  val idCounter = new AtomicInteger(0)

  def nextId: Int = idCounter.incrementAndGet()

  val defaultBoatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))

  val defaultBoardSize = 8

  def create(player1: Player, player2: Player, boardSize: Int = defaultBoardSize, allBoats: Seq[Boat] = defaultBoatSet): Game = {
    new Game(player1, player2, defaultBoardSize, allBoats, nextId)
  }
}

class Game(player1: Player, player2: Player, boardSize: Int, allBoats: Seq[Boat], id: Int) {

  type PlayerState = (Player, BoardState)

  def play: Player = {
    def playRecursively(players: (PlayerState, PlayerState)): Player = {
      val (currentPlayer, nextPlayer) = players
      if (currentPlayer.hasWon) {
        currentPlayer.player
      } else {
        val nextShot = currentPlayer.getNextShot(boardSize, currentPlayer.history)
        val newBoard = currentPlayer.processShot(nextShot)
        val updatedPlayers = (nextPlayer, (currentPlayer.player, newBoard))
        playRecursively(updatedPlayers)
      }
    }

    val player1Board: BoardState = initPlayerState(player2)
    val player1State: PlayerState = (player1, player1Board)

    val player2Board: BoardState = initPlayerState(player1)
    val player2State: PlayerState = (player2, player2Board)

    val players = (player1State, player2State)
    val winner = playRecursively(players)

    println(s"Winner: $winner")

    winner
  }

  def initPlayerState(playerToPlaceBoats: Player): BoardState = {
    val placements = playerToPlaceBoats.placeBoats(allBoats, boardSize)
    placements.foldLeft(BoardState.empty) {
      case (boardState, (boat, location)) => boardState.placeBoat(boat, location)
    }
  }

  /**
    * Demonstrate the usage of implicit conversion: when the following class declaration is in score, instances of the
    * tuple (Player, BoardState) can be automatically converted to PlayerRepr. As a result, the below defined functions
    * are added to the scope of the tuple (Player, BoardState)
    */
  implicit class PlayerStateOps(playerAndBoard: (Player, BoardState)) {
    val (player, board) = playerAndBoard

    def hasWon: Boolean = board.allShipsSunk

    def processShot(coordinate: Coordinate): BoardState = board.processShot(coordinate)

    def history: Seq[((Int, Int), ShotResult)] = board.history

    def getNextShot(boardSize: Int, shotHistory: Seq[Shot]): (Int, Int) = player.getNextShot(boardSize, shotHistory)
  }

}
