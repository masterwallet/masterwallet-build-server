package pro.masterwallet
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging


object Build extends App {

    val lastVersion: String = LastVersion.get
    println(s"Building, Last Version ${lastVersion}");

    val builder = new PackageBuilder(lastVersion)
    if (builder.isPresent) {
      println(s"Version ${lastVersion} is processed already")
    } else {
      builder.start
      println(s"Version ${lastVersion} was started")
    }
}