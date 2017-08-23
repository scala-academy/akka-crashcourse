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
import akka.http.scaladsl.server.ExceptionHandler
import spray.json.DefaultJsonProtocol

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by m06f947 on 18-8-2017.
  */

object TestJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  case class Test(size: Int, random: String)
  implicit val PortofolioFormats = jsonFormat2(Test)
}

trait ActorRoutes  {
  implicit def timeOut = Timeout(800 milliseconds)
  import battleship.routes.TestJsonSupport._
  val gameActor: ActorRef

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
        val replyF = ask(gameActor,GameStateRequest)
        onSuccess(replyF) {extraction => complete(extraction.toString)}
      }
  }
}


