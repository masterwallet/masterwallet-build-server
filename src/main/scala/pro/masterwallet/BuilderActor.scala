package pro.masterwallet
import akka.actor.{ Actor, ActorSystem, ActorLogging, Props, PoisonPill }

object BuilderActor {
  final case class ActionPerformed(description: String)
  final case class ActionFailed(description: String)
  final case object Queued
  def props: Props = Props[BuilderActor]
}

class BuilderActor extends Actor with ActorLogging {
  import BuilderActor._

  def receive: Receive = {
    case Queued => {
      log.info("Requested to be built");
      val lastVersion: String = LastVersion.get
      log.info(s"Last Version ${lastVersion}");

      val builder = new PackageBuilder(lastVersion)
      if (builder.isPresent) {
        sender() ! ActionFailed(s"Version ${lastVersion} is processed already")
      } else {
        builder.start
        sender() ! ActionPerformed(s"Version ${lastVersion} was started")
      }
    }
    case _ => log.info("BuilderActor: got an unknown message")
  }
}