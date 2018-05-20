package cloud.leslie.eventcounter

import scala.concurrent.duration._

// Stores timestamps of a particular event, up to a duration dataLifespan ago. The number of events within a given
// duration back from now can be requested.
//
// Not guaranteed to be threadsafe. For a threadsafe wrapper using Akka Actors, see EventCounterActor.
class EventCounter(private var _dataLifespan: FiniteDuration = 5.minutes) {
  import EventCounter._

  // eventTimestamps will have new timestamps appended, so will be in ascending order
  private var eventTimestamps = Vector.empty[Long]

  // removes all timestamps older than dataLifespan. This is called automatically in other functions, but can be called
  // manually (e.g. if there are many timestamps in the counter but you may not use the counter again soon you could
  // prune it after some time has passed)
  def prune(): Unit = {
    val cutoffTime = System.currentTimeMillis() - _dataLifespan.toMillis
    val index = indexOfFirstElementAtOrAboveCutoff(eventTimestamps, cutoffTime)
    eventTimestamps = eventTimestamps.drop(index)
  }

  // adds a timestamp at current time
  def addEvent(): Unit = {
    prune()
    eventTimestamps :+= System.currentTimeMillis()
  }

  // returns the number of events within duration ago. Throws IllegalArgumentException if requested duration is longer
  // than dataLifespan.
  def numberEvents(duration: Duration): Int = {
    if (duration > _dataLifespan) {
      throw new IllegalArgumentException("Cannot request longer duration than dataLifespan")
    } else {
      prune()
      val cutoffTime = System.currentTimeMillis() - duration.toMillis
      val index = indexOfFirstElementAtOrAboveCutoff(eventTimestamps, cutoffTime)
      val count = eventTimestamps.size - index
      count
    }
  }

  def isEmpty: Boolean = eventTimestamps.isEmpty

  def dataLifespan_= (newLifespan: FiniteDuration) = {
    _dataLifespan = newLifespan
    prune()
  }

  def dataLifespan = _dataLifespan

}

object EventCounter {

  // returns s.length if no element is at or above cutoff, otherwise returns index of first element at or above cutoff.
  // Assumes s is sorted and increasing.
  def indexOfFirstElementAtOrAboveCutoff[T](s: IndexedSeq[T], cutoff: T)(implicit ordering: Ordering[T]): Int = {
    import ordering._
    firstFalse(s)(ts => ts < cutoff) match {
      case None => s.length
      case Some(index) => index
    }
  }

  // Assume s is sorted, and s.map(p) looks like IndexedSeq(true, true, ..., true, false, false, ..., false).
  // This function returns the index of the first false, using a binary search, or a None if there is no false.
  def firstFalse[T](s: IndexedSeq[T])(p: T => Boolean): Option[Int] = {
    if (s.isEmpty || p(s.last)) {
      None
    } else {
      var l = 0
      var h = s.length - 1
      while (l != h) {
        val m = l + (h - l) / 2
        if (p(s(m))) {
          l = m + 1
        } else {
          h = m
        }
      }
      Some(l)
    }
  }
}
