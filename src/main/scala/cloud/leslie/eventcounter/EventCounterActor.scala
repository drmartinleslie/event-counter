package cloud.leslie.eventcounter

import akka.actor.{Actor, Props, Timers}

import scala.concurrent.duration.{Duration, _}

class EventCounterActor(initialDataLifespan: Duration) extends Actor with Timers {
  import EventCounterActor._
  val eventCounter = new EventCounter(initialDataLifespan)
  timers.startSingleTimer("prune", Prune, 1.minute)

  def receive = {
    case AddEvent =>
      eventCounter.addEvent()

    case SetDataLifespan(newLifespan) =>
      eventCounter.dataLifespan = newLifespan

    case GetDataLifespan =>
      sender ! DataLifespan(eventCounter.dataLifespan)

    case GetNumberEvents(duration) =>
      sender ! NumberEvents(eventCounter.numberEvents(duration))

    case Prune =>
      eventCounter.prune()
  }

}

object EventCounterActor {
  def props(dataLifespan: Duration = 5.minutes): Props = Props(classOf[EventCounterActor], dataLifespan)

  case object AddEvent
  case class GetNumberEvents(duration: Duration)
  case class NumberEvents(value: Int)
  case class SetDataLifespan(newLifeSpan: Duration)
  case object GetDataLifespan
  case class DataLifespan(value: Duration)
  case object Prune
}
