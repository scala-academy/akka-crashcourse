package battleship

import akka.actor.{Actor, Props}
import battleship.game.Player

/**
  * Created by jordidevos on 02/08/2017.
  */

object GameActor {

  /**
    * Received when a game needs to be player
    */
  case object Play

  /**
    * Sent back when a game is played
    */
  case class GameFinished(winner: String)

  def props(player1: Player, player2: Actor): Props = Props(new GameActor(player1, player2))
}

class GameActor(player1: Player, player2: Actor) extends Actor {
  override def receive: Receive = {
    case _ => ???
  }
}
