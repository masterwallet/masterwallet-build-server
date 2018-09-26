package pro.masterwallet
import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import scala.compat.Platform.{ currentTime }
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.atomic.AtomicBoolean

object Debounced {

  /**
   * Debounce a function i.e. it can only be invoked iff it is not already running
   * and atleast wait duration has passed since it last stopped running
   * Usage:
   *    def test(i: Int) = println(i)
   *    val f = debounce(1.second)(test)
   *    (0 to 1e9.toInt).par.map(f)
   *
   * @return a function such that it returns Some(original output) if it was invoked
   *         or else if it failed to run because of above rules, None
   */
  def start[A, B](wait: FiniteDuration)(f: A => B): A => Option[B] = {
    println("debounced.start " + currentTime.toString + " " + wait.toMillis);
    var (isRunning, lastStopTime) = (new AtomicBoolean(false), Long.MinValue)
    (input: A) => {
      val doneWaiting = (lastStopTime + wait.toMillis) <= currentTime
      if (isRunning.compareAndSet(false, doneWaiting) && doneWaiting) {
        try {
          println("debounced: returning " + currentTime.toString);
          Some(f(input))
        } finally {
          println("debounced: stopped " + currentTime.toString);
          lastStopTime = currentTime
          isRunning.set(false)
        }
      } else {
        None
      }
    }
  }
}