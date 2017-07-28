package battleship

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
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

  trait TestGameCreater extends GameCreater {
    override def createGame(id: Int): ActorRef = {
      println(s"createGame($id) in TestGameCreater")
      ActorRef.noSender
    }
  }

  import GameManagerActor._

  "GameManager" should {
    "create a game when receiving a CreateGame message and return the id" in {
      val testProbe = TestProbe()
      val testActor = system.actorOf(Props(new GameManagerActor with TestGameCreater))

      testProbe.send(testActor, CreateGame(ActorRef.noSender, ActorRef.noSender))

      testProbe.expectMsg(50 millis, "game id = 0")

    }
    "return the winner of a game after receiving a PlayGame message" in {
      ???
    }
  }

}

