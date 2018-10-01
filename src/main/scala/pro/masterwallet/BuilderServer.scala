package pro.masterwallet

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import scala.util.Properties.envOrElse


object BuilderServer extends App with BuilderRoutes {

   // set up ActorSystem and other dependencies here
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("builderServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  //#server-bootstrapping

  
  val builderActor: ActorRef = system.actorOf(BuilderActor.props, "builderActor")
  lazy val routes: Route = builderRoutes

  println("Running builder server")

  //#http-server
  val port = envOrElse("PORT", "8029").toInt
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, envOrElse("HOST", "0.0.0.0"), port)
  serverBinding.onComplete {
    case Success(bound) =>
      val slackChannel = sys.env("SLACK_CHANNEL_URL")
      println(s"Slack channel: ${slackChannel}")
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }
  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
}
