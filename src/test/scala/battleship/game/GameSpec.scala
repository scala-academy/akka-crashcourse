package battleship.game

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by jordidevos on 27/07/2017.
  */
class GameSpec extends WordSpec with Matchers {
  "Game between Linear and Stupid Random player" should {
    "be won by LinearPlayer" in {
      val player1 = new StupidRandomPlayer{override val seed = 0}
      val player2 = new LinearPlayer{}

      val game = Game.create(player1, player2)

      val winner = game.play

      winner should be(player2)
    }
  }
}
