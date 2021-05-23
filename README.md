scalm (Dave's version!)
=====

[![Join the chat at https://gitter.im/julienrf/scalm](https://badges.gitter.im/julienrf/scalm.svg)](https://gitter.im/julienrf/scalm?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Elm-inspired Scala library for writing web user interfaces **in Scala 3**.

## Installation

> THIS VERSION OF SCALM ISN'T PUBLISHED YET (Only Julian's original exists in the wild.)
> You can do a local publish.

scalm supports Scala 3 and Scala.js 1.5.1.

~~~ scala
// project/plugins.sbt
addSbtPlugin("org.scala-js"              % "sbt-scalajs"  % "1.5.1")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.17")
~~~

~~~ scala
// build.sbt
enablePlugins(ScalaJSPlugin)
libraryDependencies += "davesmith00000" %%% "scalm" % "0.0.1-SNAPSHOT"
~~~

## Overview

### Elm Architecture

Scalm provides a runtime environment for executing applications designed
according to the [Elm architecture](https://guide.elm-lang.org/architecture/).

In essence, the state of the application is modeled by an immutable `Model`,
events that change the state of the application are modeled by an immutable
`Msg` type, state transitions are implemented by a `(Msg, Model) => Model`
function, and finally, a `Model => Html[Msg]` function defines how to render
the state of the application in HTML.

### Reacting to User Input

Here is how the usual
[counter example](https://guide.elm-lang.org/architecture/user_input/buttons.html)
looks like with scalm:

~~~ scala
import scalm.{Html, Scalm}
import scalm.Html._
import org.scalajs.dom.document

object Main:
  opaque type Model = Int

  def main(args: Array[String]): Unit =
    Scalm.start(document.body, init, update, view)

  def init: Model = 0

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1

  def view(model: Model): Html[Msg] =
    div()(
      button(onClick(Msg.Decrement))(text("-")),
      div()(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

enum Msg:
  case Increment, Decrement

~~~

### Dealing With Effects

In the architecture presented above, the state of the application evolves
with DOM events but there is no way to perform HTTP requests or register a
timer. We call this kind of actions “effects”. We classify them into two
groups: commands and subscriptions. Commands let you *do* stuff, whereas
subscriptions let you register that you are interested in something.
You can find more information on effects
[here](https://guide.elm-lang.org/architecture/effects/).

Here is how the
[clock example](https://guide.elm-lang.org/architecture/effects/time.html)
looks like in scalm (using seconds so we can see movement...):

~~~ scala
import scalm.{Cmd, Html, Scalm, Sub}
import scalm.Html._
import org.scalajs.dom.document

import scalajs.js
import concurrent.duration.DurationInt

object Clock:

  opaque type Model = js.Date

  def init: (Model, Cmd[Msg]) =
    (new js.Date(), Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    (msg.newTime, Cmd.Empty)

  def view(model: Model): Html[Msg] = {
    val angle = model.getSeconds() * 2 * math.Pi / 60 - math.Pi / 2
    val handX = 50 + 40 * math.cos(angle)
    val handY = 50 + 40 * math.sin(angle)
    tag("svg")(attr("viewBox", "0, 0, 100, 100"), attr("width", "300px"))(
      tag("circle")(attr("cx", "50"), attr("cy", "50"), attr("r", "45"), attr("fill", "#0B79CE"))(),
      tag("line")(attr("x1", "50"), attr("y1", "50"), attr("x2", handX.toString), attr("y2", handY.toString), attr("stroke", "#023963"))()
    )
  }

  def subscriptions(model: Model): Sub[Msg] =
    Sub.every(1.second, "clock-ticks").map(Msg.apply)

  def main(args: Array[String]): Unit =
    Scalm.start(document.body, init, update, view, subscriptions)

final case class Msg(newTime: js.Date)
~~~

## Discussion

To my experience, correctly implementing a user interface is hard.

The very disciplined Elm programming model helps me
a lot to reason about the user interface implementation.

More specifically, in this programming model the mapping between the
state of the application and the rendered HTML is easy to follow.
Furthermore, commands and subscriptions simplify resource management
a lot (you don’t have to worry about cancelling some event handler
anymore, this is taken care of by the runtime).

On the other hand, the architecture is, by design, not extremely efficient:
on each event the entire application state is recomputed. We use a
virtual-dom technique to patch the DOM as efficiently as possible, but still,
that’s a lot of work that’s not needed with approaches like
[monadic-html](https://github.com/OlivierBlanvillain/monadic-html) or
[Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala).
