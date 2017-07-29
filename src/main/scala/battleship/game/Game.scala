package battleship.game

import com.sun.media.jfxmedia.events.PlayerStateEvent.PlayerState

/**
  * Created by jordidevos on 26/07/2017.
  */
object Game {

  var id = 0

  def nextId: Int = {
    id += 1
    id
  }

  val defaultBoatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))

  val defaultBoardSize = 8

  def create(player1: Player, player2: Player, boardSize: Int = defaultBoardSize, allBoats: Seq[Boat] = defaultBoatSet): Game = {
    new Game(player1, player2, defaultBoardSize, allBoats, nextId)
  }
}

class Game(player1: Player, player2: Player, boardSize: Int, allBoats: Seq[Boat], id: Int) {

  type PlayerState = (Player, BoardState)

  val player1Board: BoardState = initPlayerState(player2)
  val player2Board: BoardState = initPlayerState(player1)

  def initPlayerState(playerToPlaceBoats: Player): BoardState = {
    val placements = playerToPlaceBoats.placeBoats(allBoats, boardSize)
    placements.foldLeft(BoardState.empty) {
      case (boardState, (boat, location)) => boardState.placeBoat(boat, location)
    }
  }

  def play(): Player = {

    def playRecursively(players: (PlayerState, PlayerState)): Player = {
      val (currentPlayer, nextPlayer) = players
      if (currentPlayer.won) {
        currentPlayer.player
      }
      else {
        val nextShot = currentPlayer.getNextShot(boardSize, currentPlayer.history)
        val newBoard = currentPlayer.shoot(nextShot)
        val updatedPlayers = (nextPlayer, (currentPlayer.player, newBoard))
        playRecursively(updatedPlayers)
      }
    }

    val player1State: PlayerState = (player1, player1Board)
    val player2State: PlayerState = (player2, player2Board)
    val players = (player1State, player2State)

    val winner = playRecursively(players)

    println(s"Winner: $winner")

    winner
  }

  /**
    * Overkill implicit just for fun
    */
  implicit class PlayerRepr(playerAndBoard: (Player, BoardState)) {
    def player: Player = playerAndBoard._1

    def board: BoardState = playerAndBoard._2

    def won: Boolean = board.gameOver

    def shoot(coordinate: Coordinate): BoardState = board.shoot(coordinate)

    def history: Seq[((Int, Int), ShotResult)] = board.history

    def getNextShot(boardSize: Int, shotHistory: Seq[Shot]): (Int, Int) = player.getNextShot(boardSize, shotHistory)
  }

}
