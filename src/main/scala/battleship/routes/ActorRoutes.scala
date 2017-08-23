package battleship.routes

import akka.actor.ActorRef
import akka.actor.Status.{Failure, Success}
import akka.http.javadsl.model.headers.Server
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import battleship.GameActor.{GameState, GameStateRequest, StartGame}
import battleship.game.Boat
import battleship.GameActor
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, RequestTimeoutException, StatusCodes}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by m06f947 on 18-8-2017.
  */

object TestJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  case class Test(size: Int, random: String)
  implicit val testFormats: RootJsonFormat[Test] = jsonFormat2(Test)
}

trait ActorRoutes  {
  implicit def timeOut: Timeout = 20.seconds
  import battleship.routes.TestJsonSupport._
  val gameActor: ActorRef

  lazy val createGameRoute: Route =
    path("createGame") {
      post {
        entity(as[Test]) {
          test => complete {
            gameActor ! GameActor.StartGame(test.size, gameActor, gameActor, Seq(Boat(1)))
            s"Message sent to actor ${test.random}"
          }
        }
      }
    }

  def gameStateRequest(implicit ec: ExecutionContext): Route =
    path("gameState")  {
      get {
        complete {
          val state: Future[GameState] = (gameActor ? GameStateRequest).mapTo[GameState]
          state.map(_.toString)
        }
      }
  }
}


