package cloud.leslie.eventcounter

import scala.concurrent.duration.{Duration, _}

// Not thread-safe
class EventCounter(private var _dataLifespan: Duration = 5.minutes) {
  private var eventTimestamps = IndexedSeq.empty[Long]

  def addEvent(): Unit = {
    prune()
    eventTimestamps :+= System.currentTimeMillis()
  }

  def numberEvents(duration: Duration): Int = {
    if (duration > _dataLifespan) {
      throw new IllegalArgumentException("Cannot request longer duration than dataLifespan")
    } else {
      prune()
      val cutoffTime = System.currentTimeMillis() - duration.toMillis
      val count = eventTimestamps.dropWhile(_ < cutoffTime).size
      count
    }
  }

  def prune(): Unit = {
    val cutoffTime = System.currentTimeMillis() - _dataLifespan.toMillis
    eventTimestamps = eventTimestamps.dropWhile(_ < cutoffTime)
  }

  def dataLifespan_= (newLifespan: Duration) = {
    _dataLifespan = newLifespan
    prune()
  }

  def dataLifespan = _dataLifespan

}
