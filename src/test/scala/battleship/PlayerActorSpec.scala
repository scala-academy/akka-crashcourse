package battleship

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import battleship.game._
import PlayerActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by jordidevos on 28/07/2017.
  */
class PlayerActorSpec(_system: ActorSystem) extends SpecBase(_system) {

  def this() = this(ActorSystem("PlayerActorSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  trait MockPlayer extends Player {
    override def placeBoats(boats: Seq[Boat], boardSize: Int): Set[(Boat, BoatLocation)] = Set()
    override def getNextShot(boardSize: Int, shotHistory: Seq[((Int, Int), ShotResult)]): (Int, Int) = (1,2)
    override def name: String = "MyDummy"
  }


  "PlayerActor" should {
    "return the placement of boats when receiving a PlaceBoats message" in {
      val testProbe = TestProbe()
      val testActor = system.actorOf(Props(new PlayerActor with MockPlayer ))
      testProbe.send(testActor, PlaceBoats(Game.defaultBoatSet, Game.defaultBoardSize))
      testProbe.expectMsg(BoatPlacementResult(Set()))
    }
    "return the next shot upon receiving a GetNextShot message" in {
      val testProbe = TestProbe()
      val testActor = system.actorOf(Props(new PlayerActor with MockPlayer ))

      val linearPlayer = new LinearPlayer{}

      testProbe.send(testActor, GetNextShot(Game.defaultBoardSize,Seq.empty))
      testProbe.expectMsg(NextShot(1,2))

    }
    }
}

