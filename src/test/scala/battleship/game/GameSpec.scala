package battleship.game

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by jordidevos on 27/07/2017.
  */
class GameSpec extends WordSpec with Matchers {
  "Game between Linear and Stupid Random player" should {
    "be won by LinearPlayer" in {
      val player1 = new StupidRandomPlayer(0)
      val player2 = LinearPlayer
      val game = Game.create(player1, player2)

      game.play()

      game.winner should be(Some(player2))
    }
  }
}
