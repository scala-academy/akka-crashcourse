package battleship

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import battleship.GameActor.props
import battleship.routes._

import scala.io.StdIn

class BaseRoute (inputGameActor: ActorRef) extends BaseRoutes with ActorRoutes with SimpleRoutes {
  var gameActor = inputGameActor
}

object WebServer extends Directives{
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val gameActor = system.actorOf(GameActor.props) //should be replaced by gamemanager when that is ready ready
    val baseRouteObject = new BaseRoute(gameActor)

    // Here you can define all the different routes you want to have served by this web server.
    // Note that routes might be defined in separated traits like the current case. You have to add the traits to the BaseRoute class as well.

    val routes = baseRouteObject.baseRoutes  ~ baseRouteObject.simpleRoutes ~ baseRouteObject.createGameRoute ~ baseRouteObject.gameStateRequest

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }



}
