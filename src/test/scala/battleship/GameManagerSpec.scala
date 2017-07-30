package battleship

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import GameManagerActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by jordidevos on 28/07/2017.
  */
class GameManagerSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("GameManagerSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "GameManager" should {
    "create a game when receiving a CreateGame message and return the id" in {
      val testProbe = TestProbe()
      var testActorCalled = false
      val testActor = system.actorOf(Props(new GameManagerActor with GameActorCreator {
        override def createGameActor: ActorRef = {
          testActorCalled = true
          ActorRef.noSender
        }
      }))

      testProbe.send(testActor, CreateGame(ActorRef.noSender, ActorRef.noSender))

      testProbe.expectMsg(500 millis, GameCreated(0))
      testActorCalled should be(true)
    }
    "return the winner of a game after receiving a PlayGame message" in {
      // TODO
    }
  }

}

