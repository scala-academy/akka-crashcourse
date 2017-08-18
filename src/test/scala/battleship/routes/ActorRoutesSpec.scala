package battleship.routes
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport.defaultNodeSeqUnmarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import battleship.GameActor.{GameNotStartedYet, Uninitialised}
import battleship.{GameActor, SpecBase}
import org.scalatest.{Matchers, WordSpec}

import scala.xml.NodeSeq


class ActorRoutesSpec extends WordSpec with Matchers with ScalatestRouteTest with ActorRoutes {
  val testProbe = TestProbe()
  var gameActor = testProbe.ref


  "ActorRoutes" should {
    "send a message to gameActor when creategame is posted, and return a message sent message" in {
      //todo
      }
    "return a message sent message when creategame is posted" in {
        //todo
      }

    "return a timeout when the gamestate get request is posted to an actor that does not respond" in {
      Get("/gameState") ~> gameStateRequest ~> check {
        status shouldBe StatusCodes.InternalServerError
      }
    }
    "return the gamestate when the gamestate get request is posted" in {
      gameActor = system.actorOf(Props(new GameActor))
      Get("/gameState") ~> gameStateRequest ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe "state = " + GameNotStartedYet(Uninitialised)
      }
    }
  }
}
