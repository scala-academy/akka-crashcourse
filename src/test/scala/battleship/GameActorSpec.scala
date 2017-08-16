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

    "Game should send msg to ask for boat placement from both players when started" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testProbe = TestProbe()
      val testActorgame = system.actorOf(props)
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8

      testProbe.send(testActorgame, StartGame(size, testplayer1.ref, testplayer2.ref,boatSet))
      testplayer1.expectMsg(PlaceBoats(boatSet, size))
      testplayer2.expectMsg(PlaceBoats(boatSet, size))
    }
    "Game should be started when placement has been received from both players" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement1 = Set((Boat(1),game.BoatLocation(1,1,true)))
      val placement2 = Set((Boat(1),game.BoatLocation(1,2,true)))
      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor
      val boardStates = Map(testplayer1.ref -> actor.placeBoats(placement1),
        testplayer2.ref -> actor.placeBoats(placement2))

      actor.context.become(actor.gameInit(1, testplayer1.ref, testplayer2.ref))
      testplayer1.send(actorRef, BoatSetup(placement1))
      testplayer2.send(actorRef, BoatSetup(placement2))
      testProbe.send(actorRef, GameStateRequest)
      testProbe.expectMsg(GameStarted(boardStates))
    }
    "Game should ask player 2 for the first move after game is setup" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))
      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor
      val boardStates = Map(testplayer1.ref -> actor.placeBoats(placement),
        testplayer2.ref -> actor.placeBoats(placement))
      actor.context.become(actor.gameInit(1,testplayer1.ref,testplayer2.ref,Map(testplayer1.ref->actor.placeBoats(placement))))
      testplayer2.send(actorRef, BoatSetup(placement))
      testplayer2.expectMsg(GetNextShot(1, boardStates(testplayer2.ref).history))
    }
    "Game should switch players and ask for the next move after a move is done" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))
      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor
      val boardstate = BoardState(placement,List())
      val boardStates = Map(testplayer1.ref -> boardstate,testplayer2.ref -> boardstate)
      val size = 8

      actor.context.become(actor.gameStarted(testplayer1.ref,8,testplayer1.ref,testplayer2.ref,testplayer2.ref,boardStates))
      testplayer2.send(actorRef,Move(2,2))
      testplayer1.expectMsg(GetNextShot(size, boardStates(testplayer1.ref).history))
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
      testplayer2.expectMsg(GameEnded(testplayer2.ref))
    }
    "If asked for gamestatus it should give it in every gamestate" in {
      //Todo, and implicit in 'Play the games given various commands' 'unit' test
    }
    "Finish the game correctly with 1-tilegame" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val actorRef = TestActorRef[GameActor]
      val actor = actorRef.underlyingActor
      val placement = Set((Boat(1),game.BoatLocation(1,1,true)))
      //(1,1 should be 0,0?????
      val boatSet = Seq(Boat(1))
      val size = 1
      val boardStates = Map(testplayer1.ref -> actor.placeBoats(placement),
        testplayer2.ref -> actor.placeBoats(placement))

      testProbe.send(actorRef, StartGame(size, testplayer1.ref, testplayer2.ref,boatSet))
      testplayer1.expectMsg(PlaceBoats(boatSet, size))
      testplayer2.expectMsg(PlaceBoats(boatSet, size))
      testplayer1.send(actorRef, BoatSetup(placement))
      testplayer2.send(actorRef, BoatSetup(placement))
      testplayer2.expectMsg(GetNextShot(1, boardStates(testplayer2.ref).history))
      testplayer2.send(actorRef, Move(1,1))
      testplayer2.expectMsg(GameEnded(testplayer2.ref))
      testplayer1.expectMsg(GameEnded(testplayer2.ref))
    }
  }
}