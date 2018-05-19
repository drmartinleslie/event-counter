import EventCounterActor._
import akka.actor.{Actor, Props}

import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.util.Try

class EventCounterActor(initialDataLifespan: Duration) extends Actor {
  private val eventCounter = new EventCounter(initialDataLifespan)

  def receive = {
    case AddEvent =>
      eventCounter.addEvent()

    case SetDataLifespan(newLifespan) =>
      eventCounter.dataLifespan = newLifespan

    case GetDataLifespan =>
      sender ! DataLifespan(eventCounter.dataLifespan)

    case GetNumberEvents(duration) =>
      sender ! NumberEvents(eventCounter.numberEvents(duration))

  }

}

object EventCounterActor {
  def props(dataLifespan: Duration = 5.minutes): Props = Props(classOf[EventCounterActor], dataLifespan)

  case object AddEvent
  case class GetNumberEvents(duration: Duration)
  case class NumberEvents(result: Try[Int])
  case class SetDataLifespan(newLifeSpan: Duration)
  case object GetDataLifespan
  case class DataLifespan(value: Duration)
}
