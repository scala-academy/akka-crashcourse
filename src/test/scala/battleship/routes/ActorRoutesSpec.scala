package battleship.routes
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport.defaultNodeSeqUnmarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActorRef, TestProbe}
import akka.util.Timeout
import battleship.GameActor.Uninitialised
import battleship.GameManagerActor.StartManager
import battleship.game.{Boat, Game}
import battleship.{GameActor, GameManagerActor, PlayerActor, SpecBase}
import battleship.GameManagerActor._
import battleship.routes.StartManagerJsonSupport._
import org.scalatest.{Matchers, WordSpec}

import scala.xml.NodeSeq
import scala.concurrent.duration._


class ActorRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with ActorRoutes {
  val testProbe = TestProbe()
  val gameManagerActor: ActorRef = testProbe.ref
  val player1: ActorRef = testProbe.ref
  val player2: ActorRef = testProbe.ref

  override def timeOut: Timeout = 100.millisecond
  "ActorRoutes" should {
    "send a message that manager is started when startmanager is used" in {
      }
    "return a message gamestarted when a playgameis issued with an id" in {
      Post("/playGame?gameid=1", StartManagerEndPoint(8,Seq(5,4,3,2))) ~> playGameRoute ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldBe s"Startgame sent to game with id '1'"
      }
        //todo
      }

    "return a timeout when the creategameroute is posted to an actor that does not respond" in {
      Post("/createGame") ~> createGameRoute ~> check {
        status shouldEqual StatusCodes.InternalServerError
      }
    }
  }
}

class ActorRoutesSpec2 extends WordSpec with Matchers with ScalatestRouteTest with ActorRoutes {
  //uses the real gamemanageractor
  val testProbe = TestProbe()
  val gameManagerActor: ActorRef = system.actorOf(Props(new GameManagerActor))
  val player1: ActorRef = testProbe.ref
  val player2: ActorRef = testProbe.ref
  val size2: Int = Game.defaultBoardSize
  val boats: Seq[Boat] =Game.defaultBoatSet

  gameManagerActor ! StartManager(size2 ,boats)
//tried to use context.become, but private definitions is preventing me from using it

  "ActorRoutes" should {
    "return the gameid when the creategame is posted" in {
      Post("/createGame") ~> createGameRoute ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe "0"
      }
    }
  }
}
