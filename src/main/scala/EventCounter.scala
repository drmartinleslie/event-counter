import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

// Not thread-safe
class EventCounter(private var _dataLifespan: Duration = 5.minutes) {
  private var eventTimestamps = IndexedSeq.empty[Long]

  def addEvent(): Unit = {
    prune()
    eventTimestamps :+= System.currentTimeMillis()
  }

  def numberEvents(duration: Duration): Try[Int] = {
    if (duration > _dataLifespan) {
      Failure(new IllegalArgumentException("Cannot request longer duration than dataLifespan"))
    } else {
      prune()
      val cutoffTime = System.currentTimeMillis() - duration.toMillis
      val count = eventTimestamps.dropWhile(_ < cutoffTime).size
      Success(count)
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
