package battleship

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import battleship.GameManagerActor._
import battleship.game.Boat

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

  val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
  val boardSize = 8

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

      testProbe.send(testActor, StartManager(boardSize, boatSet))
      testProbe.send(testActor, CreateGame(ActorRef.noSender, ActorRef.noSender))

      testProbe.expectMsgPF(500 millis) { case GameCreated(_) => }

      testActorCallCount.get should be(1)
    }

    "return the winner of a game after receiving a PlayGame message" in {
      val testProbe = TestProbe()
      val gameProbe = TestProbe()

      val testActor = system.actorOf(Props(new GameManagerActor {
        // inject the gameProbe
        override def createGameActor: ActorRef = gameProbe.ref
      }))

      testProbe.send(testActor, StartManager(boardSize, boatSet))
      testProbe.send(testActor, CreateGame(ActorRef.noSender, ActorRef.noSender))

      val gameID: Int = testProbe.expectMsgPF() {
        case GameManagerActor.GameCreated(x) => x
      }

      testProbe.send(testActor, PlayGame(gameID))
      val playerRef = TestProbe("player").ref
      gameProbe.send(testActor, GameActor.GameEnded(playerRef))

      testProbe.expectMsg(GameManagerActor.GameEnded(playerRef.toString()))
    }
  }
}

