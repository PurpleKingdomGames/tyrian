scalm (Dave's version!)
=====

[![Join the chat at https://gitter.im/julienrf/scalm](https://badges.gitter.im/julienrf/scalm.svg)](https://gitter.im/julienrf/scalm?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Elm-inspired Scala library for writing web user interfaces **in Scala 3**.

> **This is a fork of [Scalm](https://github.com/julienrf/scalm)**, dusted off, cleaned up, and brought up to date.

## Installation

> THIS VERSION OF SCALM ISN'T PUBLISHED YET (Only Julian's original exists in the wild.)
> You can do a local publish.

scalm supports Scala 3 and Scala.js 1.5.1.

~~~ scala
// project/plugins.sbt
addSbtPlugin("org.scala-js"              % "sbt-scalajs"  % "1.5.1")
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

### Examples

There are several examples in the examples folder to help get you started.

**Running the examples**
To run them you will need to have yarn (or npm) installed.

On first run:

```sh
yarn install
```

...and from then on:

```sh
yarn start
```

Then navigate to [http://localhost:1234/](http://localhost:1234/)

**Things to know about the examples**
The examples are useful but before you wholesale copy one to use as a starting point.

1. They refer to the underlying Scalm sbt project, so you'll need to add a proper library dependency.

2. They are built weirdly, because they're designed to be built and run as a one-liner alongside the main project.
    - The examples use parcel to do the heavy lifting involved with pulling the project together, which is fine, you could also use webpack or whatever you prefer. However, if you look at the "start" script, you'll see it invokes `sbt fastOptJS && (..)`, which you should not do. If you run parcel and leave it running (yarn start, minus the sbt bit), and re-run `fastOptJS` as you would during normal development, parcel will see the file change (as it would any other resource) and reload the site for you immediately - MUCH FASTER!
    - If you'd like to keep the current build arrangement you see in the "start" scripts, it might be nicer to switch from sbt to Mill or sbt's thin client. They may be better suited to this build arrangement with lots of cold starts.

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

If you're new to the territory, I highly recommend [this thoughtful blog post](https://dev.to/raquo/my-four-year-quest-for-perfect-scala-js-ui-development-b9a) by [Laminar](https://laminar.dev/)'s author, Nikita Gazarov.

There is a point in that post where Nikita says the following:

> (..) and having now walked the path myself I finally understood exactly what that reason was: functional reactive programming (FRP) and virtual DOM don't mix!
> 
> Virtual DOM and FRP solve the exact same problem – efficiently keeping the rendered DOM in sync with application state – but they approach it from entirely opposite directions (..)

..and he's entirely correct. It's an important fork in the road. One direction takes you to FRP, the other to VirtualDom. Libraries like Laminar are the former and Scalm and Elm are the latter. Both are equally valid choices with quite subtle trade-offs.

Broadly, the argument for FRP is speed, as updates are minimal and precise. The argument for VirtualDom is that it's easier to test and reason about.

You don't have to look hard to find counter arguments to both positions thought: Elm is blazing fast, and Laminar has solved the classic diamond problem. But that's the general argument.

In the end, it's mostly personal preference.
