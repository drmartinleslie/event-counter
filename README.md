# Event Counter

Event Counter is a simple Scala library for counting how many times an event has occurred in a recent time period.

## Installing

### Option 1: Fetch dependency from github

Add the following to your build.sbt

```scala
lazy val root = (project in file(".")).dependsOn(eventCounter)
lazy val eventCounter = RootProject(uri("https://github.com/drmartinleslie/event-counter.git"))
```

### Option 2: Release locally and fetch from ivy cache
Run `sbt releaseLocal` in this project and then in the project in which you desire to use the Event Counter library, add the following to your build.sbt
```scala
libraryDependencies ++= Seq(
  "cloud.leslie" %% "event-counter" % "1.0"
)

```

## Basic Usage

There are two different classes you can use to count events.

### Using EventCounter

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

One note is that automatic pruning of data older than `dataLifespan` will only occur when addEvent or eventCounter are called. If you want to manually prune (for example if you stop calling the counter and want to reduce its memory usage) you can call `eventCounter.prune()`


### Using EventCounterActor

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
val result = (eventCounterActor ? GetNumberEvents(1.minutes)).mapTo[NumberEvents]

result.onComplete {
case Success(NumberEvents(value)) =>
  println(s"There were $value events in the last 1 minutes")
  system.terminate()
case Failure(e) =>
  e.printStackTrace()
  system.terminate()
}
```

EventCounterActor never requires manual pruning.

## The Data Lifespan

EventCounter has a variable `dataLifespan` that can be set in the constructor and altered later. Data older than that duration will be pruned whenever eventCounter is used. If this variable is set then data will be pruned based on this new value.

This variable also can be accessed through EventCounterActor via the messages `GetDataLifespan` and `SetDataLifespan`.