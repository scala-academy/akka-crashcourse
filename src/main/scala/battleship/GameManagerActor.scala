package battleship

import akka.actor.{Actor, ActorRef, Props}
import battleship.GameManagerActor.GameCreated

/**
  * Created by jordidevos on 28/07/2017.
  */
object GameManagerActor {

  case class CreateGame(player1: ActorRef, player2: ActorRef)

  /**
    * Returned when a new game is created. The returned id should be unique for this game
    *
    * @param id
    */
  case class GameCreated(id: Int)

  case class PlayGame(id: Int)

  /**
    * Returned when a game is finished
    */
  case class GameEnded(winner: String)

  def props: Props = Props(new GameManagerActor with GameCreatorImpl)

}

class GameManagerActor extends Actor {
  this: GameActorCreator =>

  override def receive: Receive = {
    case _ => {
      val _ = createGameActor
      sender() ! GameCreated(0)
    }
  }

}
