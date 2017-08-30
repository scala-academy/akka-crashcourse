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
import battleship.GameManagerActor.{CreateGame, GameCreated, PlayGame, StartManager}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by m06f947 on 18-8-2017.
  */

object StartManagerJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  case class StartManagerEndPoint(size: Int, boats: Seq[Int])
  implicit val StartManagerFormats: RootJsonFormat[StartManagerEndPoint] = jsonFormat2(StartManagerEndPoint)
}

trait ActorRoutes {
  implicit def timeOut: Timeout = 20.seconds

  import battleship.routes.StartManagerJsonSupport._

  val gameManagerActor: ActorRef
  val player1: ActorRef
  val player2: ActorRef

  def createGameRoute(implicit ec: ExecutionContext): Route =
    path("createGame") {
      post {
        complete {
          val gameid = (gameManagerActor ? CreateGame(player1, player2)).mapTo[GameCreated]
          gameid.map(_.id.toString)
        }
      }
    }

  def playGameRoute: Route =
    path("playGame") { //add gameid to path
      post {
        parameters('gameid.as[Int])
        { gameid =>
          complete {
            gameManagerActor ! PlayGame(gameid)
            s"Startgame sent to game with id '$gameid'"
          }
        }
      }
    }

  def startManagerRoute: Route =
    path("startManager"){
      post {
        entity(as[StartManagerEndPoint]) {
          startcommand => complete {
            val boatSet = startcommand.boats.map(size => Boat(size)) //Using this because sprayjson doesnt know Boats.
            gameManagerActor ! StartManager(startcommand.size,boatSet)
            "Message sent to start gamemanager"
          }

        }
      }
    }

}
