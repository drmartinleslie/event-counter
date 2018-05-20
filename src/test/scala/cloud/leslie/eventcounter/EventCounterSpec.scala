package cloud.leslie.eventcounter
import org.scalatest._
import EventCounter._

import scala.concurrent.duration._

class EventCounterSpec extends FlatSpec with Matchers {
  "EventCounter" should "return 0 events when initialized" in {
    val eventCounter = new EventCounter()
    eventCounter.numberEvents(1.minute) shouldBe 0
  }

  it should "return 2 events after they are added" in {
    val eventCounter = new EventCounter()
    eventCounter.addEvent()
    eventCounter.addEvent()
    eventCounter.numberEvents(1.minute) shouldBe 2
  }

  it should "not count events beyond requested duration" in {
    val eventCounter = new EventCounter()
    eventCounter.addEvent()
    eventCounter.addEvent()
    Thread.sleep(2.seconds.toMillis)
    eventCounter.addEvent()
    eventCounter.numberEvents(1.seconds) shouldBe 1
  }

  it should "disallow requesting beyond dataLifespan" in {
    val eventCounter = new EventCounter(2.seconds)
    val result = intercept[IllegalArgumentException] { eventCounter.numberEvents(1.minute) }
    result.getMessage shouldBe "Cannot request longer duration than dataLifespan"
  }

  it should "respect an altered dataLifespan" in {
    val eventCounter = new EventCounter(2.seconds)
    eventCounter.dataLifespan = 5.minutes
    noException should be thrownBy { eventCounter.numberEvents(1.minute) }
  }

  "indexOfFirstElementAtOrAboveCutoff" should "return correct index" in {
    val s = Vector(1, 2, 3, 4)
    val result = indexOfFirstElementAtOrAboveCutoff(s, 3)
    result shouldBe 2
  }

}