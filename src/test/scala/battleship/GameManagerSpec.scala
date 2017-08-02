package battleship

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import GameManagerActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by jordidevos on 28/07/2017.
  */
class GameManagerSpec(_system: ActorSystem) extends SpecBase(_system) {

  def this() = this(ActorSystem("GameManagerSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "GameManager" should {
    "create a game when receiving a CreateGame message and return the id" in {
      val testProbe = TestProbe()
      val testActorCallCount = new AtomicInteger(0)
      val testActor = system.actorOf(Props(new GameManagerActor with GameActorCreator {
        override def createGameActor: ActorRef = {
          testActorCallCount.incrementAndGet()
          ActorRef.noSender
        }
      }))

      testProbe.send(testActor, CreateGame(ActorRef.noSender, ActorRef.noSender))

      testProbe.expectMsg(500 millis, GameCreated(0))
      testActorCallCount.get should be(1)
    }
    "return the winner of a game after receiving a PlayGame message" in {
      // TODO
    }
  }

}

