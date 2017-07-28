package battleship

import akka.actor.{Actor, ActorRef, Props}

/**
  * Created by jordidevos on 28/07/2017.
  */
object GameManagerActor {

  case class CreateGame(player1: ActorRef, player2: ActorRef)

  /**
    * Returned when a new game is created. The returned id should be unique for this game
    * @param id
    */
  case class GameCreated(id: Int)

  case class PlayGame(id: Int)

  /**
    * Returned when a game is finished
    */
  case class GameEnded(winner: String)

  def props: Props = Props(new GameManagerActor with GameCreaterImpl)

}

class GameManagerActor extends Actor {
  this: GameCreater =>

  override def receive: Receive = {
    case _ => {

      createGame(0)

      sender() ! "game id = 0"
    }
  }

}

/**
  * The following trait is used to inject the logic of creating games into the GameManagerActor. This allows other
  * logic to be injected for testing purposes
  */
trait GameCreater {
  def createGame(id: Int): ActorRef
}

trait GameCreaterImpl extends GameCreater {
  def createGame(id: Int): ActorRef = {

    // TODO create game actor here

    println(s"Created game $id")

    ActorRef.noSender // TODO Fix: return appropriate ActorRef
  }
}