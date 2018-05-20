package cloud.leslie.eventcounter
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import cloud.leslie.eventcounter.EventCounterActor._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._

class EventCounterActorSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "EventCounterActor" should {

    val eventCounterActor = system.actorOf(EventCounterActor.props())

    "add and return some events" in {
      eventCounterActor ! AddEvent
      eventCounterActor ! AddEvent
      eventCounterActor ! GetNumberEvents(2.minutes)
      expectMsg(NumberEvents(2))
    }

    "set and get dataLifespan" in {
      eventCounterActor ! SetDataLifespan(2.minutes)
      eventCounterActor ! GetDataLifespan
      expectMsg(DataLifespan(2.minutes))
    }
  }

}

