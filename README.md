[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Ftyrian%2Ftags)](https://github.com/PurpleKingdomGames/tyrian/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.com/channels/716435281208672356)

# Tyrian

Elm-inspired Scala UI library.

## Provenance, and a note of thanks from Dave.

Tyrian was originally a fork of [Scalm](https://github.com/julienrf/scalm) by [Julien Richard-Foy](https://github.com/julienrf).

Scalm was the Scala.js library I'd been looking for but found too late, and it's great fun! In my opinion it was simply ahead of its time, and alas the original authors and contributors had moved on to pastures new long before it was brought to my attention.

I decided to fork it and re-release it under a new name, partly because I wanted to take it in my own direction without corrupting the original work, and partly ...because I just wasn't sure how to pronounce Scalm! (I did ask!)

Scalm/Tyrian and [Indigo](https://github.com/PurpleKingdomGames/indigo) (which I also look after) are kindred spirits, in that they both follow the TEA pattern (The Elm Architecture), which is the only frontend architecture pattern I'm interested in these days.

I hope to use Tyrian to complement Indigo, and so have brought it in under the same organisation.

Tyrian is Scalm with the cobwebs blown off. All it's libraries are up to date, I've started expanding the API, and it will only ever be released against Scala 3.

With huge thanks to the original developers,

Dave, 5th June 2021

### Why "Tyrian"?!

> "It took tens of thousands of desiccated hypobranchial glands, wrenched from the calcified coils of spiny murex sea snails before being dried and boiled, to colour even a single small swatch of fabric, whose fibres, long after staining, retained the stench of the invertebrate's marine excretions. Unlike other textile colours, whose lustre faded rapidly, Tyrian purple ... only intensified with weathering and wear – a miraculous quality that commanded an exorbitant price, exceeding the pigment's weight in precious metals." ~ [BBC](https://www.bbc.com/culture/article/20180801-tyrian-purple-the-regal-colour-taken-from-mollusc-mucus)

So it's a purple dye that smells of where it came from and gets richer over time with use. Perfect.

## Installation

> Not yet published!

Tyrian supports Scala 3 and Scala.js 1.5.1.

~~~ scala
// project/plugins.sbt
addSbtPlugin("org.scala-js" % "sbt-scalajs"  % "1.5.1")
~~~

~~~ scala
// build.sbt
enablePlugins(ScalaJSPlugin)
libraryDependencies += "io.indigoengine" %%% "tyrian" % "0.1.0-SNAPSHOT"
~~~

## Overview

### Elm Architecture

Tyrian provides a runtime environment for executing applications designed
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

1. They refer to the underlying Tyrian sbt project, so you'll need to add a proper library dependency.

2. They are built weirdly, because they're designed to be built and run as a one-liner alongside the main project.
    - The examples use parcel to do the heavy lifting involved with pulling the project together, which is fine, you could also use webpack or whatever you prefer. However, if you look at the "start" script, you'll see it invokes `sbt fastOptJS && (..)`, which you should not do. If you run parcel and leave it running (yarn start, minus the sbt bit), and re-run `fastOptJS` as you would during normal development, parcel will see the file change (as it would any other resource) and reload the site for you immediately - MUCH FASTER!
    - If you'd like to keep the current build arrangement you see in the "start" scripts, it might be nicer to switch from sbt to Mill or sbt's thin client. They may be better suited to this build arrangement with lots of cold starts.

### Reacting to User Input

Here is how the usual
[counter example](https://guide.elm-lang.org/architecture/user_input/buttons.html)
looks like with tyrian:

~~~ scala
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
looks like in tyrian (using seconds so we can see movement...):

~~~ scala
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
~~~

## Discussion

If you're new to the territory, I highly recommend [this thoughtful blog post](https://dev.to/raquo/my-four-year-quest-for-perfect-scala-js-ui-development-b9a) by [Laminar](https://laminar.dev/)'s author, Nikita Gazarov.

There is a point in that post where Nikita says the following:

> (..) and having now walked the path myself I finally understood exactly what that reason was: functional reactive programming (FRP) and virtual DOM don't mix!
> 
> Virtual DOM and FRP solve the exact same problem – efficiently keeping the rendered DOM in sync with application state – but they approach it from entirely opposite directions (..)

...and that is entirely correct in my opinion. It's an important fork in the road. One direction takes you to FRP and Laminar, the other to Virtual DOM like Tyrian and Elm. Both are equally valid choices with quite subtle trade-offs.

Broadly the argument for FRP is speed, as updates are minimal and precise. The argument for Virtual DOM is that it's easier to test and reason about.

However, You don't have to look hard to find counter arguments to both positions: Elm is blazing fast, and Laminar has solved the classic diamond problem. ...but that's the general argument.

In the end, it's mostly personal preference.
