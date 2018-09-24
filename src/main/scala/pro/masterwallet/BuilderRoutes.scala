package pro.masterwallet

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import pro.masterwallet.UserRegistryActor._
import akka.pattern.ask
import akka.util.Timeout
import ujson._

trait BuilderRoutes { 

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  lazy val log = Logging(system, classOf[BuilderRoutes])

  // other dependencies that UserRoutes use
  // def userRegistryActor: ActorRef
  // Required by the `ask` (?) method below
  // implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  lazy val builderRoutes: Route =
    path("push") {
      get {
        // val users: Future[Users] = (userRegistryActor ? GetUsers).mapTo[Users]
        complete(ujson.write(Js.Obj("result" -> "ok", "action" -> "pushed")))
      }
    }
  //#all-routes
}
