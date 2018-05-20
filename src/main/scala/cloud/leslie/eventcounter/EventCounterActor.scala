package cloud.leslie.eventcounter

import akka.actor.{Actor, Props, Timers}

import scala.concurrent.duration._

class EventCounterActor(initialDataLifespan: FiniteDuration) extends Actor with Timers {
  import EventCounterActor._
  val eventCounter = new EventCounter(initialDataLifespan)
  val TimerKey = "Prune"

  def receive = {
    case AddEvent =>
      eventCounter.addEvent()
      if (!timers.isTimerActive(TimerKey)) {
        timers.startSingleTimer(TimerKey, Prune, eventCounter.dataLifespan)
      }

    case SetDataLifespan(newLifespan) =>
      eventCounter.dataLifespan = newLifespan

    case GetDataLifespan =>
      sender ! DataLifespan(eventCounter.dataLifespan)

    case GetNumberEvents(duration) =>
      sender ! NumberEvents(eventCounter.numberEvents(duration))

    case Prune =>
      eventCounter.prune()
      if (!eventCounter.isEmpty && !timers.isTimerActive(TimerKey)) {
        timers.startSingleTimer(TimerKey, Prune, eventCounter.dataLifespan)
      }
  }

}

object EventCounterActor {
  def props(dataLifespan: FiniteDuration = 5.minutes): Props = Props(classOf[EventCounterActor], dataLifespan)

  case object AddEvent
  case class GetNumberEvents(duration: FiniteDuration)
  case class NumberEvents(value: Int)
  case class SetDataLifespan(newLifeSpan: FiniteDuration)
  case object GetDataLifespan
  case class DataLifespan(value: FiniteDuration)
  case object Prune
}
