package battleship

/**
  * Created by m06f947 on 3-8-2017.
  */

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game.{BoardState, Boat, Game, LinearPlayer}
import battleship.routes.gameActor
import battleship.routes.gameActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by jordidevos on 28/07/2017.
  */
class gameActorSpec(_system: ActorSystem)
  extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

    def this() = this(ActorSystem("GameManagerSpecActorSystem"))

    override def afterAll: Unit = {
      shutdown(system)
    }
  "gameActor" should {
    "Play the games given various commands" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testActorgame = system.actorOf(props)
      val placement = LinearPlayer.placeBoats(Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2)),8)



      testProbe.send(testActorgame, StartGame(8, testplayer1.ref, testplayer2.ref,Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))))
      testProbe.expectMsgPF(500 millis) {
        case "Game started!" => {
          println("gameactortest: game started")
        }
      }
      testplayer1.expectMsgPF(500 millis) {
        case PlaceBoats(a,b) => {
          println("gameactor sent message to actor 1 asking for boat placement")
        }
      }
      testplayer1.send(testActorgame, ThisIsBoatSetup(placement))
      testplayer2.expectMsgPF(500 millis) {
        case PlaceBoats(a,b) => {
          println("gameactor sent message to actor 2 asking for boat placement")
        }
      }
      testplayer2.send(testActorgame, ThisIsBoatSetup(placement))
      testplayer2.expectMsgPF(500 millis) {
        case GetNextShot(a,b) => {
          println("gameactor sent message to actor 2 asking for next shot")
        }
      }
      testplayer2.send(testActorgame, ThisIsNextMove(1,1))
      testplayer1.expectMsgPF(500 millis) {
        case GetNextShot(a,b) => {
          println("gameactor sent message to actor 1 asking for next shot")
        }
      }
    }
    "Finish the game correctly" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testActorgame = system.actorOf(props)
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))


      testProbe.send(testActorgame, StartGame(1, testplayer1.ref, testplayer2.ref,Seq(Boat(1))))
      testplayer1.expectMsgPF(500 millis) {
        case _ => //
      }
      testplayer1.send(testActorgame, ThisIsBoatSetup(placement))
      testplayer2.expectMsgPF(500 millis) {
        case _ => //
      }
      testplayer2.send(testActorgame, ThisIsBoatSetup(placement))
      testplayer2.expectMsgPF(500 millis) {
        case _ => //
      }
      testplayer2.send(testActorgame, ThisIsNextMove(1,1))
      testplayer2.expectMsgPF(500 millis) {
        case string: String => {
          println(string)
          val stringcomp = "Player " + testplayer2.ref +  " has won!!"
          if (string == stringcomp) succeed
          else fail("Game not ended properly!")
        }
      }

    }
  }
}

