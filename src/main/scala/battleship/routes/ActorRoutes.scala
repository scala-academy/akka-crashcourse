package battleship.routes

import akka.actor.ActorRef
import akka.http.javadsl.model.headers.Server
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import battleship.GameActor.{GameNotStartedYet, GameStateRequest, StartGame}
import battleship.game.Boat
import battleship.GameActor
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, RequestTimeoutException, StatusCodes}
import akka.http.scaladsl.server.ExceptionHandler
import spray.json.DefaultJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by m06f947 on 18-8-2017.
  */

object TestJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  case class Test(size: Int, random: String)
  implicit val PortofolioFormats = jsonFormat2(Test)
}
/*
actorref geeft een probleem, maar die ga ik uiteindelijk toch niet gebruiken omdat we voor de gamemanageractor gaan
object StartGameJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PortofolioFormats = jsonFormat4(StartGame)
}
*/

trait ActorRoutes  {

  import battleship.routes.TestJsonSupport._
  var gameActor: ActorRef

  lazy val createGameRoute =
    path("createGame") {
      post {
        entity(as[Test]) {
          test =>
            gameActor ! GameActor.StartGame(test.size,gameActor,gameActor,Seq(Boat(1)))
            complete("Message sent to actor" + test.random)
        }
      }
    }
  lazy val gameStateRequest =
    path("gameState")  {
      get {
        implicit val timeout:Timeout = Timeout(1 seconds)
        val replyF = gameActor ? GameStateRequest
        val result = Await.result(replyF,timeout.duration)
        complete("state = " + result)
      }
  }
}


