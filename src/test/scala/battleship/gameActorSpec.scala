package battleship

/**
  * Created by m06f947 on 3-8-2017.
  */

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._
import battleship.GameActor.{GameNotStartedYet, _}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by jordidevos on 28/07/2017.
  */
class GameActorSpec(_system: ActorSystem) extends SpecBase(_system) {

    def this() = this(ActorSystem("GameActorSpecActorSystem"))

    override def afterAll: Unit = {
      shutdown(system)
    }
  "gameActor" should {

    "Game should send started message after being started and ask for setup from players 1 and 2" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testProbe = TestProbe()
      val testActorgame = system.actorOf(props)
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))


      testProbe.send(testActorgame, StartGame(8, testplayer1.ref, testplayer2.ref,Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))))
      testProbe.expectMsgPF(500 millis) {
        case "Game started!" => {
          println("gameactortest: game started")
        }
      }
      testplayer1.expectMsgPF(500 millis) {
        case PlaceBoats(boatset, size) => {
          println("Player1 asked for setup")
        }
      }
      testplayer2.expectMsgPF(500 millis) {
        case PlaceBoats(boatset, size) => {
          println("Player2 asked for setup")
        }
      }
    }
    "Game should send msg to ask for boat placement from both players when started" in {
     val testplayer1 = TestProbe()
     val testplayer2 = TestProbe()
     val placement1 = Set((Boat(1),game.BoatLocation(1,1,true)))
     val placement2 = Set((Boat(1),game.BoatLocation(1,2,true)))

     val actorRef = TestActorRef[GameActor]
     val actor = actorRef.underlyingActor
     actor.context.become(actor.gameInit(1, testplayer1.ref, testplayer2.ref))
     testplayer1.send(actorRef, BoatSetup(placement1))
     testplayer2.send(actorRef, BoatSetup(placement2))
     testplayer1.expectMsg(ReceivedPlacement(BoatSetup(placement1)))
     testplayer2.expectMsg(ReceivedPlacement(BoatSetup(placement2)))
   }
    "Game should ask player 2 for the first move after game is setup" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))
      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor

      actor.context.become(actor.gameInit(1,testplayer1.ref,testplayer2.ref,Map(testplayer1.ref->actor.placeBoats(placement))))
      testplayer2.send(actorRef, BoatSetup(placement))
      testplayer2.expectMsg(ReceivedPlacement(BoatSetup(placement)))
      testplayer2.expectMsgPF(500 millis) {
        case GetNextShot(size, history) => {
          println("gameactor sent message to actor 2 asking for shot")
        }
      }
      testplayer1.send(actorRef,GameStateRequest)
      testplayer1.expectMsgPF(500 millis) {
        case a => println(a)
      }
    }
    "Game should switch players and ask for the next move after a move is done" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))

      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor

      val boardstate = BoardState(Set((Boat(1),BoatLocation(1,1,true))),List())
      actor.context.become(actor.gameStarted(testplayer1.ref,8,testplayer1.ref,testplayer2.ref,testplayer2.ref,Map(testplayer1.ref -> boardstate,testplayer2.ref -> boardstate)))
      testplayer2.send(actorRef,Move(2,2))
      testplayer1.expectMsgPF(500 millis) {
        case GetNextShot(size, history) => succeed
      }
    }
    "If the game receives the final sink, the game should end" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement = Set((Boat(1),game.BoatLocation(0,0,true)))
      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor
      val boardstate = BoardState(Set((Boat(1),BoatLocation(0,0,true))),List())
      actor.context.become(actor.gameStarted(testplayer1.ref,1,testplayer1.ref,testplayer2.ref,testplayer2.ref,Map(testplayer1.ref -> boardstate,testplayer2.ref -> boardstate)))
      testplayer2.send(actorRef,Move(0,0))
      testplayer2.expectMsgPF(500 millis) {
        case a:String => if (a.startsWith("The game ended")) succeed
      }

    }
    "If asked for gamestatus it should give it in every gamestate" in {
       //Todo, and implicit in 'Play the games given various commands' 'unit' test
    }
    "Finish the game correctly with 1-tilegame" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testActorgame = system.actorOf(props)
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))
      //(1,1 should be 0,0?????

      testProbe.send(testActorgame, StartGame(1, testplayer1.ref, testplayer2.ref,Seq(Boat(1))))
      testplayer1.expectMsgPF() {
        case PlaceBoats(_,_) => println("player 1 boat placed")
      }
      testplayer2.expectMsgPF(){
        case PlaceBoats(_,_) => println("player 2 boat placed")
      }
      testplayer1.send(testActorgame, BoatSetup(placement))
      testplayer1.expectMsg(ReceivedPlacement(BoatSetup(placement)))
      testplayer2.send(testActorgame, BoatSetup(placement))
      testplayer2.expectMsg(ReceivedPlacement(BoatSetup(placement)))
      testplayer2.expectMsgPF() {
        case GetNextShot(_,_) => println("player fired shot")
      }
      testplayer2.send(testActorgame, Move(1,1))
      testplayer2.expectMsgPF() {
        case string: String => {
          println(string)
          val stringcomp = "Player " + testplayer2.ref +  " has won!!"
          string should be(stringcomp)
        }
      }
      testplayer1.expectMsgPF() {
        case string: String => {
          println(string)
          val stringcomp = "Player " + testplayer2.ref +  " has won!!"
          string should be(stringcomp)
        }
      }
    }
    "Play the games given various commands" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testActorgame = system.actorOf(props)
      val placement = LinearPlayer.placeBoats(Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2)),8)


      testProbe.send(testActorgame,GameStateRequest)
      testProbe.expectMsg(GameNotStartedYet(Uninitialised))
      testProbe.send(testActorgame, StartGame(8, testplayer1.ref, testplayer2.ref,Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))))
      testProbe.expectMsg("Game started!")
      testplayer1.expectMsgPF(){
        case PlaceBoats(_,_) => println("player 1 boats placed")
      }
      testplayer2.expectMsgPF(){
        case PlaceBoats(_,_) => println("player 2 boats placed")
      }
      testProbe.send(testActorgame,GameStateRequest)
      testProbe.expectMsg(GameNotStartedYet(PlayerAskedForBoats))
      testplayer1.send(testActorgame, BoatSetup(placement))
      testplayer1.expectMsg(ReceivedPlacement(BoatSetup(placement)))
      testProbe.send(testActorgame,GameStateRequest)
      testProbe.expectMsg(GameNotStartedYet(PlayerAskedForBoats))
      testplayer2.send(testActorgame, BoatSetup(placement))
      testplayer2.expectMsg(ReceivedPlacement(BoatSetup(placement)))
      testplayer2.expectMsgPF() {
        case GetNextShot(_,_) => println("Shot requested from player 2")
      }
      testProbe.send(testActorgame,GameStateRequest)
      testProbe.expectMsgPF() {
        case GameStarted(_) => println("Game started")
      }

      testplayer2.send(testActorgame, Move(1,1))
      testplayer1.expectMsgPF() {
        case GetNextShot(_,_) => println("Shot requested from player 1")
      }
      testplayer1.send(testActorgame, Move(1,1))
      testplayer2.send(testActorgame, Move(2,1))
      testplayer1.send(testActorgame, Move(1,2))
      testplayer2.send(testActorgame, Move(3,1))


      testProbe.send(testActorgame,GameStateRequest)
      testProbe.expectMsgPF(500 millis) {
        case GameStarted(boardstates) => {
          boardstates(testplayer2.ref).history.size should be (3)
          boardstates(testplayer1.ref).history.size should be (2)
          println("Boardstates have correct number of moves in it")
        }
      }
    }
  }
}