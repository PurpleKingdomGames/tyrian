---
title: "Subscriptions"
---

## Subscriptions: A notification of change

Subscriptions (`Sub`s) are used to observe something that changes over time, and to emit discrete messages when something happens.

For example, we could chose to observe the mouse position, and emit a message every time the mouse moves, like this:

```scala
import org.scalajs.dom.document
import org.scalajs.dom.MouseEvent

import tyrian.*

type Model = ???

enum Msg:
  case MouseMove(x: Double, y: Double)

val mousePosition: Sub[Msg] = 
  Sub.fromEvent("mousemove", document) { case e: MouseEvent  =>
    Option(Msg.MouseMove(e.pageX, e.pageY))
  }

def subscriptions(model: Model): Sub[Msg] =
  mousePosition
```

A change in mouse position will now result in a `MouseMove` message that will be piped back to your `updateModel` function.

Subscriptions can be used on their own, or in conjunction with commands to form a pub/sub relationship with a resource, as is the case with web sockets and the tyrian-indigo bridge.

### Working with Subscriptions

Subscriptions are Functors which means that you can `map` over them to change the resultant message. They are also Monoids which means that they have an empty representation `Sub.empty` and that you can `combine` them together, using `combine` or the shorthand operator: `sub1 |+| sub2`.

A common thing to need to do is batch multiple subs together into a single subscription, like this:

```scala
import scala.concurrent.duration._

import org.scalajs.dom.document
import org.scalajs.dom.MouseEvent

import tyrian.*

enum Msg:
  case MouseMove(x: Double, y: Double)
  case CurrentSeconds(seconds: Double)

val mousePosition: Sub[Msg] = 
  Sub.fromEvent("mousemove", document) { case e: MouseEvent  =>
    Option(Msg.MouseMove(e.pageX, e.pageY))
  }

val tick =
  Sub.every(1.second, "tick")
    .map(date => Msg.CurrentSeconds(date.getSeconds()))

def subscriptions(model: Model): Sub[Msg] =
  Sub.Batch(
    mousePosition,
    tick
  )
```
