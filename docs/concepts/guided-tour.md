---
title: "A Guided Example"
menuTitle: "Guided Example"
---

The normal use case for Tyrian is to build a Single Page App/Application (SPA). You can also use Tyrian for Server-side Rendering (SSR), but this page will focus on SPAs.

## A guided example

Let's walk through an example to see what goes into a Tyrian App.

The 'counter' is a very common example you'll come across for many frameworks, it's a handy Rosetta stone for when you need to orientate yourself in a new framework quickly.

The example is comprised of two buttons, `+` and `-`, and some text that shows a count that goes up and dow when you press the buttons.

### Counter Code

The version of this in the [examples](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples) is already quite lean, but the version below has been stripped back to the minimum.

```scala mdoc:silent
import tyrian.Html.*
import tyrian.*
import cats.effect.IO

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (0, Cmd.None)

  def update(msg: Msg, model: Model): (Model, Cmd[IO, Msg]) =
    msg match
      case Msg.Increment => (model + 1, Cmd.None)
      case Msg.Decrement => (model - 1, Cmd.None)

  def view(model: Model): Html[Msg] =
    div()(
      button(onClick(Msg.Decrement))("-"),
      div()(model.toString),
      button(onClick(Msg.Increment))("+")
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

type Model = Int

enum Msg:
  case Increment, Decrement
```

Lets go through it...

#### `TyrianApp`

```scala
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:
```

Here we have the most common imports that bring in all the basics you'll need to build your SPA.

All Tyrian SPAs must extend `TyrianApp` which is parameterized by a message type and a model type. These types can be anything you like, but typically `Msg` is an enum or ADT, and `Model` is probably a case class (in our case we're just using an `Int`, but we'll come back to that).

Extending `TyrianApp[Msg, Model]` will produce helpful compile errors that will tell you all the functions you need to implement, i.e. `init`, `update`, `view` and `subscriptions`.

The other thing you must do is export the app using Scala.js's `@JSExportTopLevel("TyrianApp")`. You can call it anything you like, but all the examples expect the name "TyrianApp".

#### The model

```scala
type Model = Int
```

Our app is a counter, so we need a number we can increment and decrement. In this super simple example, an `Int` is all that we need for our whole model. Normally you'd probably have a `case class` or something instead. To make it fit nicely, we've allocated our `Int` to a `Model` type alias.

> The version in the examples uses an opaque type, but here we've reduced it to a type alias.

To use our model, we're going to have to initialize it!

```scala mdoc:reset:invisible
import tyrian.Html.*
import tyrian.*
import cats.effect.IO

type Model = Int
```

```scala mdoc:silent
  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (0, Cmd.None)
```

There's a few things going on here, the only bit we _really_ care about here is the `0` because that is going to be the starting value of our 'model'.

Some of the other things you can see here:

- `flags` - Flags can be passed into the app at launch time, think of them like command line arguments.
- `Cmd[Msg]` - Commands aren't used in the example, but they allow you to capture and run side effects and emit resulting events. They are a requirement for the function signature, and here we satisfy that with `Cmd.None`.

#### Rendering the page

Let's draw the page. All the functions in Tyrian are encouraged to be pure, which means they operate solely on their arguments to produce a value.

The `view` takes the latest immutable (read-only) model, and produces some HTML in the form of `Html[Msg]`.

```scala mdoc:silent
  def view(model: Model): Html[Msg] =
    div(
      button("-"),
      div(model.toString),
      button("+")
    )
```

Here we make a div, add a `-` button, the another div containing the count (i.e. the model) as plain text, and finally another `+` button. If you're familiar with HTML this should all look pretty familiar.

If you wanted to add an `id` attribute to the div, you would do so like this:

```scala
div(id := "my container")(...)
```

Of course a button isn't much use unless it does something, and what we can do is emit an event, called a message, when the button is clicked. For that we need to declare our message type which we'll do as a simple enum that represents the two actions we want to perform:

```scala mdoc:silent
enum Msg:
  case Increment, Decrement
```

...and add our click events:

```scala mdoc:reset:invisible
import tyrian.Html.*
import tyrian.*
import cats.effect.IO

type Model = Int
enum Msg:
  case Increment, Decrement
```

```scala mdoc:silent
  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))("-"),
      div(model.toString),
      button(onClick(Msg.Increment))("+")
    )
```

> Note the return type of view is `Html[Msg]`. This is because unlike normal JavaScript, the `onClick` is not directly instigating a normal callback, the HTML elements are mapped through and produce messages as values that are passed back to Tyrian.

#### Updating the counter's value

The final thing we need to do is react to the messages the view is sending, as follows:

```scala mdoc:silent
  def update(msg: Msg, model: Model): (Model, Cmd[IO, Msg]) =
    msg match
      case Msg.Increment => (model + 1, Cmd.None)
      case Msg.Decrement => (model - 1, Cmd.None)
```

Recall that our 'model' is just a type alias for an `Int`, so all we do is match on the `Msg` enum type, and either increment or decrement the model - done!

#### Subscriptions

Subscriptions are part of the standard requirements, but this example doesn't use them for anything. They allow you to "subscribe" to processes that emit events over time.
