import org.scalatest._

import scala.concurrent.duration._
import org.scalatest.TryValues._

class EventCounterSpec extends FlatSpec with Matchers {
  "EventCounter" should "return 0 events when initialized" in {
    val eventCounter = new EventCounter()
    eventCounter.numberEvents(1.minute).success.value shouldBe 0
  }

  it should "return 2 events after they are added" in {
    val eventCounter = new EventCounter()
    eventCounter.addEvent()
    eventCounter.addEvent()
    eventCounter.numberEvents(1.minute).success.value shouldBe 2
  }

  it should "not count events beyond requested duration" in {
    val eventCounter = new EventCounter()
    eventCounter.addEvent()
    eventCounter.addEvent()
    Thread.sleep(2.seconds.toMillis)
    eventCounter.addEvent()
    eventCounter.numberEvents(1.seconds).success.value shouldBe 1
  }

  it should "disallow requesting beyond dataLifespan" in {
    val eventCounter = new EventCounter(2.seconds)
    val result = eventCounter.numberEvents(1.minute).failure.exception
    result shouldBe an[IllegalArgumentException]
    result.getMessage shouldBe "Cannot request longer duration than dataLifespan"
  }
}