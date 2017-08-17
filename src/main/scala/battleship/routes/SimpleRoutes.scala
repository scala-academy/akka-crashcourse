package battleship.routes

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.{get, put}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import battleship.GameActor.{GameStateRequest, StartGame}
import battleship.game.Boat
import battleship.GameActor
import akka.util.Timeout
import battleship.routes.testJsonSupport.Test
import spray.json.DefaultJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration._

object testJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  case class Test(size: Int, random: String)
  implicit val PortofolioFormats = jsonFormat2(Test)
}

/**
 * Routes can be defined in separated classes like shown in here
 */
trait SimpleRoutes {
  var gameActor: ActorRef
  //GameActor.StartGame


  // This `val` holds one route (of possibly many more that will be part of your Web App)
  lazy val simpleRoutes =
    path("hello") { // Listens to paths that are exactly `/hello`
      get { // Listens only to GET requests
        complete("Say hello to akka-http") // Completes with some html page
      }
    }
  lazy val createGameRoute =
    path("createGame") {
      put {
        entity(as[Test]) {
          (test: Test) =>
            gameActor ! GameActor.StartGame(test.size,gameActor,gameActor,Seq(Boat(1)))
            complete("Message sent to actor")
        }
      }
    }
  lazy val gameStateRequest =
    path("gameState") {
      get {
        //parameter
        implicit val timeout:Timeout = Timeout(3 seconds)
        val replyF = gameActor ? GameStateRequest
        val result = Await.result(replyF,timeout.duration)
        complete("state = " + result)
      }
    }


}
