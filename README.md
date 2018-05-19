# Event Counter

Event Counter is a simple Scala library for counting how many times an event has occurred in a recent time period.

## Fetch dependency from github

Add the following to your build.sbt

```scala
lazy val root = (project in file(".")).dependsOn(eventCounter)
lazy val eventCounter = RootProject(uri("https://github.com/drmartinleslie/event-counter.git"))
```

## Release locally and fetch from ivy cache
Run `sbt releaseLocal` in this project and then in the project which you desire to use the Event Counter library, add the following to your build.sbt
```scala
libraryDependencies ++= Seq(
  "cloud.leslie" %% "event-counter" % "1.0"
)

```

## Using EventCounter

EventCounter is not thread safe. It is intended for use in single threaded applications.

Example usage:

```scala
import cloud.leslie.eventcounter._
import concurrent.scala.duration._


val eventCounter = new EventCounter()
eventCounter.addEvent()
eventCounter.addEvent()
val result = eventCounter.numberEvents(1.minute)
println(s"$numEvents events in last minute")
```
One note is that pruning will only occur when addEvent or eventCounter are called. If you want to manually prune you can call `eventCounter.prune()`


## Using EventCounterActor

EventCounterActor is intended for use in multithreaded applications using Akka actors.

Example usage:
```scala
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import cloud.leslie.eventcounter.EventCounterActor._
import cloud.leslie.eventcounter._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

val system = ActorSystem("eventcounter-system")
implicit val timeout: Timeout = 10.seconds
val eventCounterActor = system.actorOf(EventCounterActor.props(), "eventcounter")
eventCounterActor ! AddEvent
eventCounterActor ! AddEvent
val result = (eventCounterActor ? GetNumberEvents(17.minutes)).mapTo[NumberEvents]

result.onComplete {
case Success(NumberEvents(value)) =>
  println(s"There were $value events in the last 1 minutes")
  system.terminate()
case Failure(e) =>
  e.printStackTrace()
  system.terminate()
}
```