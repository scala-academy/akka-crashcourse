package battleship

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import battleship.game.Game
import PlayerActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by jordidevos on 28/07/2017.
  */
class PlayerActorSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("GameManagerSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "PlayerActor" should {
    "return the placement of boats when receiving a PlaceBoats message" in {
      val testProbe = TestProbe()
      val testActor = system.actorOf(props)

      testProbe.send(testActor, PlaceBoats(Game.defaultBoatSet, Game.defaultBoardSize))

      testProbe.expectMsgPF(50 millis) {
        case BoatPlacementResult(boatLocations) => // TODO: handling of PlaceBoats to be implemented
      }

    }
    "return the next shot upon receiving a GetNextShot message" in {
      // TODO
    }
  }
}

