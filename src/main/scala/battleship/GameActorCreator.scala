package battleship

import akka.actor.ActorRef

/**
  * The following trait is used to inject the logic of creating game actors into the GameManagerActor. This allows other
  * logic to be injected for testing purposes
  */
trait GameActorCreator {
  def createGameActor: ActorRef
}

trait GameCreatorImpl extends GameActorCreator {
  def createGameActor: ActorRef = {
    // TODO create and return game actor here
    ActorRef.noSender
  }
}