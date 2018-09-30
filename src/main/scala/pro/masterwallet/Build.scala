package pro.masterwallet
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging


object Build extends App {

    val lastVersion: String = LastVersion.get
    println(s"Building, Last Version ${lastVersion}");

}