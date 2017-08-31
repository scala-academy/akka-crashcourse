package battleship

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.TestProbe
import battleship.PersistentFSMGameActor._
import battleship.PlayerActor.{GetNextShot, PlaceBoats}
import battleship.game._

/**
  * Created by M06F947 on 31-8-2017.
  */
class PersistentFSMGameActorSpec (_system: ActorSystem) extends SpecBase(_system) {
  def this() = this(ActorSystem("PersistentFSMGameActorSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "FSMgameActor" should {

    "Game should send msg to ask for boat placement from both players when started" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testProbe = TestProbe()
      val testActorgame = system.actorOf(PersistentFSMGameActor.props())
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8

      testProbe.send(testActorgame, GameStateRequest)
      testProbe.expectMsg((Uninitialised,GameData(0, 0, Map(),Map())))
      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.expectMsg(PlaceBoats(boatSet, size))
      testplayer2.expectMsg(PlaceBoats(boatSet, size))
      testProbe.send(testActorgame, GameStateRequest)
      testProbe.expectMsg((WaitingForBoatPlacement,GameData(size, 0, Map(0 -> PlayerState(BoardState.empty, testplayer1.ref), 1 -> PlayerState(BoardState.empty, testplayer2.ref)), Map(testplayer1.ref -> 0, testplayer2.ref -> 1))))
    }
    "Game should be started when placement has been received from both players" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement1 = Set((Boat(1),game.BoatLocation(1,1,true)))
      val placement2 = Set((Boat(1),game.BoatLocation(1,2,true)))
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8
      val testActorgame = system.actorOf(PersistentFSMGameActor.props())

      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.send(testActorgame, BoatSetup(placement1))
      testplayer2.send(testActorgame, BoatSetup(placement2))
      testProbe.send(testActorgame, GameStateRequest)
      testProbe.expectMsg((RunningGame,GameData(8,0,Map(0 -> PlayerState(BoardState(Set((Boat(1),BoatLocation(1,2,true))),List()),testplayer1.ref), 1 -> PlayerState(BoardState(Set((Boat(1),BoatLocation(1,1,true))),List()),testplayer2.ref)),Map(testplayer1.ref -> 0, testplayer2.ref -> 1))))
    }
    "Game should ask player 2 for the first move after game is setup" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement1 = Set((Boat(1),game.BoatLocation(1,1,true)))
      val placement2 = Set((Boat(1),game.BoatLocation(1,2,true)))
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8
      val testActorgame = system.actorOf(PersistentFSMGameActor.props())

      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.send(testActorgame, BoatSetup(placement1))
      testplayer2.send(testActorgame, BoatSetup(placement2))
      testplayer1.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(GetNextShot(size, Seq()))
    }
    "Game should switch players and ask for the next move after a move is done" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement1 = Set((Boat(1),game.BoatLocation(1,1,true)))
      val placement2 = Set((Boat(1),game.BoatLocation(1,2,true)))
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8
      val testActorgame = system.actorOf(PersistentFSMGameActor.props())

      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.send(testActorgame, BoatSetup(placement1))
      testplayer2.send(testActorgame, BoatSetup(placement2))
      testplayer1.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(GetNextShot(size, Seq())) //need to catch message else it cant take the next one
      testplayer2.send(testActorgame,Move(2,2))
      testplayer1.expectMsg(GetNextShot(size, Seq()))
    }
    "If the game receives the final sink, the game should end" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement = Set((Boat(1),game.BoatLocation(0,0,true)))
      val testActorgame = system.actorOf(PersistentFSMGameActor.props())
      val size = 1
      val boatSet = Seq(Boat(1))
      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.send(testActorgame, BoatSetup(placement))
      testplayer2.send(testActorgame, BoatSetup(placement))
      testplayer1.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(GetNextShot(size, Seq())) //need to catch message else it cant take the next one
      testplayer2.send(testActorgame,Move(0,0))
      testplayer2.expectMsg(GameEnded)
      testProbe.send(testActorgame, GameStateRequest)
      testProbe.expectMsg((GameEnded,GameData(1,1,Map(1 -> PlayerState(BoardState(Set((Boat(1),BoatLocation(0,0,true))),List()),testplayer2.ref), 0 -> PlayerState(BoardState(Set((Boat(1),BoatLocation(0,0,true))),List(((0,0),Sink(Boat(1))))),testplayer1.ref)),Map(testplayer1.ref -> 0, testplayer2.ref -> 1))))
    }
    "Game should come back online and have the correct status when crashed" in {
      val testProbe = TestProbe()
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val placement1 = Set((Boat(1),game.BoatLocation(1,1,true)))
      val placement2 = Set((Boat(1),game.BoatLocation(1,2,true)))
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8
      val testActorgame = system.actorOf(PersistentFSMGameActor.props())

      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.send(testActorgame, BoatSetup(placement1))
      testplayer2.send(testActorgame, BoatSetup(placement2))
      testplayer1.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(PlaceBoats(boatSet, size)) //need to catch message else it cant take the next one
      testplayer2.expectMsg(GetNextShot(size, Seq())) //need to catch message else it cant take the next one
      testplayer2.send(testActorgame,Move(2,2))
      testplayer1.expectMsg(GetNextShot(size, Seq()))

      val gamedata = (RunningGame,GameData(8,1,Map(1 -> PlayerState(BoardState(Set((Boat(1),BoatLocation(1,1,true))),List()),testplayer2.ref), 0 -> PlayerState(BoardState(Set((Boat(1),BoatLocation(1,2,true))),List(((2,2),Miss))),testplayer1.ref)),Map(testplayer1.ref -> 0, testplayer2.ref -> 1)))
      testProbe.send(testActorgame, GameStateRequest)
      testProbe.expectMsg(gamedata)
      testActorgame ! PoisonPill
      val testActorgame2 = system.actorOf(PersistentFSMGameActor.props("akka://PersistentFSMGameActorSpecActorSystem/user/$f"))
      testProbe.send(testActorgame2, GameStateRequest)
      testProbe.expectMsg(gamedata)

    }


  }
}