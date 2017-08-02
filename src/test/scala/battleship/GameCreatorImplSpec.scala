package battleship

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Jordi on 30-7-2017.
  */
class GameCreatorImplSpec(_system: ActorSystem) extends SpecBase(_system) {

  def this() = this(ActorSystem("GameManagerSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "GameCreatorImpl" should {
    "create a new GameActor when createGameActor is called" in {
      val creator = new GameCreatorImpl {}

      val result = creator.createGameActor

      result should be(ActorRef.noSender) // TODO fix test to reflect desired behavior
    }
  }
}
