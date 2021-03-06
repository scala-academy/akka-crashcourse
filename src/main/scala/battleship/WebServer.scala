package battleship

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import battleship.GameActor.props
import battleship.routes._

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

class BaseRoute (inputGameActor: ActorRef, playerOne: ActorRef, playerTwo: ActorRef) extends BaseRoutes with ActorRoutes {
  val gameManagerActor: ActorRef = inputGameActor
  val player1: ActorRef = playerOne
  val player2: ActorRef = playerTwo
}

object WebServer extends Directives{
  def main(args: Array[String]) {

    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val gameActor = system.actorOf(GameActor.props) //should be replaced by gamemanager when that is ready ready
    val player1 = system.actorOf(PlayerActor.linearPlayerProps)
    val player2 = system.actorOf(PlayerActor.stupidRandomPlayerProps)
    val baseRoute = new BaseRoute(gameActor,player1, player2)

    // Here you can define all the different routes you want to have served by this web server.
    // Note that routes might be defined in separated traits like the current case. You have to add the traits to the BaseRoute class as well.

    val routes = baseRoute.baseRoutes ~ baseRoute.createGameRoute ~ baseRoute.startManagerRoute ~ baseRoute.playGameRoute

    val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }



}
