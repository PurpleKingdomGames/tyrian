+++
title = "Examples"
menuTitle = "Examples"
weight = 3
+++

## Examples

There are several examples in the examples folder to help get you started.

### Running the examples

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

### Things to know about the examples

The examples are useful but before you wholesale copy one to use as a starting point.

1. They refer to the underlying Tyrian sbt project, so you'll need to add a proper library dependency.

2. They are built weirdly, because they're designed to be built and run as a one-liner alongside the main project.
    - The examples use parcel to do the heavy lifting involved with pulling the project together, which is fine, you could also use webpack or whatever you prefer. However, if you look at the "start" script, you'll see it invokes `sbt fastOptJS && (..)`, which you should not do. If you run parcel and leave it running (yarn start, minus the sbt bit), and re-run `fastOptJS` as you would during normal development, parcel will see the file change (as it would any other resource) and reload the site for you immediately - MUCH FASTER!
    - If you'd like to keep the current build arrangement you see in the "start" scripts, it might be nicer to switch from sbt to Mill or sbt's thin client. They may be better suited to this build arrangement with lots of cold starts.

### Reacting to User Input

Here is what the standard Elm
[counter example](https://elm-lang.org/examples/buttons)
looks like in Tyrian:

```scala
import tyrian.{Html, Tyrian}
import tyrian.Html._
import org.scalajs.dom.document

object Main:
  opaque type Model = Int

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view)

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
```

### Dealing With Effects

In the architecture presented above, the state of the application evolves
with DOM events but there is no way to perform HTTP requests or register a
timer. We call this kind of actions “effects”. We classify them into two
groups: commands and subscriptions. Commands let you *do* stuff, whereas
subscriptions let you register that you are interested in something.
You can find more information on effects
[here](https://guide.elm-lang.org/effects/).

Here is what the Elm
[clock example](https://elm-lang.org/examples/clock)
looks like in Tyrian (using seconds):

```scala
import tyrian.{Cmd, Html, Tyrian, Sub}
import tyrian.Html._
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
    tag("svg")(attributes("viewBox" -> "0, 0, 100, 100", "width" -> "300px"))(
      tag("circle")(attributes("cx" -> "50", "cy" -> "50", "r" -> "45", "fill" -> "#0B79CE"))(),
      tag("line")(
        attributes(
          "x1"     -> "50",
          "y1"     -> "50",
          "x2"     -> handX.toString,
          "y2"     -> handY.toString,
          "stroke" -> "#023963"
        )
      )()
    )
  }

  def subscriptions(model: Model): Sub[Msg] =
    Sub.every(1.second, "clock-ticks").map(Msg.apply)

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view, subscriptions)

final case class Msg(newTime: js.Date)
```
