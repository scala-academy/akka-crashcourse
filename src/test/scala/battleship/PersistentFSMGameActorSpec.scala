package battleship

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import battleship.PersistentFSMGameActor.InitGame
import battleship.PlayerActor.PlaceBoats
import battleship.game.Boat

/**
  * Created by M06F947 on 31-8-2017.
  */
class PersistentFSMGameActorSpec (_system: ActorSystem) extends SpecBase(_system) {
  def this() = this(ActorSystem("PersistentFSMGameActorSpecActorSystem"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  "gameActor" should {

    "Game should send msg to ask for boat placement from both players when started" in {
      val testplayer1 = TestProbe()
      val testplayer2 = TestProbe()
      val testProbe = TestProbe()
      val testActorgame = system.actorOf(PersistentFSMGameActor.props)
      val boatSet = Seq(Boat(5), Boat(4), Boat(3), Boat(3), Boat(2))
      val size = 8

      testProbe.send(testActorgame, InitGame(size, testplayer1.ref, testplayer2.ref, boatSet))
      testplayer1.expectMsg(PlaceBoats(boatSet, size))
      testplayer2.expectMsg(PlaceBoats(boatSet, size))
    }


  }
}