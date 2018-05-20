package cloud.leslie.eventcounter

import akka.actor.{Actor, Props, Timers}

import scala.concurrent.duration._

// A wrapper around EventCounter, allowing threadsafe access. Also has automatic pruning.
class EventCounterActor(initialDataLifespan: FiniteDuration) extends Actor with Timers {
  import EventCounterActor._
  val eventCounter = new EventCounter(initialDataLifespan)

  def receive = {
    case AddEvent =>
      eventCounter.addEvent()
      // Pruning strategy: whenever an event is added, prune it dataLifespan later.
      // If there is already a pruning timer, then when that prune occurs, if there
      // is still events in the counter, we will start another pruning timer.
      if (!timers.isTimerActive(PruneTimerKey)) {
        timers.startSingleTimer(PruneTimerKey, Prune, eventCounter.dataLifespan)
      }

    case Prune =>
      eventCounter.prune()
      if (!eventCounter.isEmpty && !timers.isTimerActive(PruneTimerKey)) {
        timers.startSingleTimer(PruneTimerKey, Prune, eventCounter.dataLifespan)
      }

    case SetDataLifespan(newLifespan) =>
      eventCounter.dataLifespan = newLifespan

    case GetDataLifespan =>
      sender ! DataLifespan(eventCounter.dataLifespan)

    case GetNumberEvents(duration) =>
      sender ! NumberEvents(eventCounter.numberEvents(duration))


  }

}

object EventCounterActor {
  def props(dataLifespan: FiniteDuration = 5.minutes): Props = Props(classOf[EventCounterActor], dataLifespan)

  val PruneTimerKey = "Prune"

  case object AddEvent
  case class GetNumberEvents(duration: FiniteDuration)
  case class NumberEvents(value: Int)
  case class SetDataLifespan(newLifeSpan: FiniteDuration)
  case object GetDataLifespan
  case class DataLifespan(value: FiniteDuration)
  case object Prune
}
