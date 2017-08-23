package examples

import akka.actor.SupervisorStrategy.Escalate
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.pattern.ask
import akka.util.Timeout
import battleship.SpecBase
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

class SupervisionExample extends SpecBase(ActorSystem("HBP")) with ScalaFutures{

  class Child extends Actor {

    override def preStart(): Unit = {
      println("PRESTART")
      super.preStart()
    }

    override def postStop(): Unit = {
      println("POSTSTOP")
      super.postStop()
    }

    def receive: Receive = {
      case "crash" => throw new RuntimeException("")
      case m       => println(m)
    }
  }

  class Parent extends Actor {
    def receive: Receive = {
      case "create" => sender() ! context.actorOf(Props(new Child))
      case m        => println(m)
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
      case r: RuntimeException if r.getMessage == "" => Escalate
    }
  }

  "it" should {
    "work" in {
      val p = system.actorOf(Props(new Parent))
      implicit val t: Timeout = 2.seconds
      val c: ActorRef = (p ? "create").mapTo[ActorRef].futureValue

      c ! "iets"
      c ! "crash"
      c ! "crash"
      p ! "piets"
    }
  }


}
