package battleship.game

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

  var player1Board: BoardState = BoardState.empty
  var player2Board: BoardState = BoardState.empty

  var winner: Option[Player] = None

  def play(): Unit = {
    var players: Seq[(Player, BoardState)] = initPlayers

    def currentPlayer = players.head

    while (!currentPlayer.won) {
      val nextShot = currentPlayer.getNextShot(boardSize, currentPlayer.history)
      val newBoard = currentPlayer.shoot(nextShot)
      players = players.tail ++ Seq((currentPlayer.player, newBoard))
    }

    winner = Some(currentPlayer.player)

    println(s"Winner: ${winner.get}")
  }

  def initPlayers: Seq[(Player, BoardState)] = {
    val player1Placements = player1.placeBoats(allBoats, boardSize)
    val player2Placements = player2.placeBoats(allBoats, boardSize)

    for ((boat, location) <- player2Placements) player1Board = player1Board.placeBoat(boat, location)
    for ((boat, location) <- player1Placements) player2Board = player2Board.placeBoat(boat, location)

    Seq((player1, player1Board), (player2, player2Board))
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
