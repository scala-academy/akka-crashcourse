package battleship

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import battleship.GameActor.props
import battleship.routes._

import scala.io.StdIn

object WebServer extends Directives{
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    var gameActor = system.actorOf(GameActor.props) //should be replaced by gamemanager when ready
    val baseobject = new BaseRoutes(gameActor)

    val routes = baseobject.baseRoutes  ~ baseobject.simpleRoutes ~ baseobject.createGameRoute ~ baseobject.gameStateRequest

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  // Here you can define all the different routes you want to have served by this web server
  // Note that routes might be defined in separated traits like the current case


}
