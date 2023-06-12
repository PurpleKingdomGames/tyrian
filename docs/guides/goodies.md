---
title: "Built-in Goodies"
menuTitle: "Goodies"
---

Tyrian comes with a number of handy functions built-in that you can make use of and explore:

## Built-in `Cmd` goodies

These nuggets of functionality are used as commands.

- `Dom` - A few methods such as `focus` and `blur` to manipulate the DOM. Inspired by the Elm [Browser.Dom](https://package.elm-lang.org/packages/elm/browser/latest/Browser.Dom) package.
- `FileReader` - Given the id of a file input field that has had a file selected, this Cmd will read either an image or text file to return an `HTMLImageElement` or `String` respectively.
- `Http` - Make HTTP requests that return their responses as a message.
- `HotReload` - During development, autosave and restore your model. Pick up where you left off between site reloads.
- `ImageLoader` - Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
- `LocalStorage` - Allows you to save and load to/from your browsers local storage.
- `Logger` - A simple logger that logs to the Browsers console with a few standard headers and the log message.
- `Random` - A Cmd to generate random values.

In the examples below, we will be using this catch-all ADT, here it is for reference.

```scala mdoc:js:shared
import org.scalajs.dom.html

enum Msg:
  case Read(contents: String)
  case UseImage(img: html.Image)
  case RandomValue(value: String)
  case Error(message: String)
  case Empty
  case NoOp
```

### `Dom`

Assuming two messages `Error` and `Empty`, we can attempt to focus a given ID.

```scala mdoc:js:shared
import cats.effect.IO
import tyrian.*
import tyrian.cmds.*

val cmd: Cmd[IO, Msg] =
  Dom.focus("my-id") {
    case Left(Dom.NotFound(id)) => Msg.Error(s"ID $id not found")
    case Right(_) => Msg.Empty
  }
```

`Dom.blur` works in the same way, though, performing the opposite effect.

### `FileReader`

Will read any file data, with build in support for text and images.

Assuming two messages `Error` and `Read`, we can attempt to read the contents of a text file.

```scala mdoc:js
import tyrian.cmds.*

val cmd: Cmd[IO, Msg] =
  FileReader.readText("my-file-input-field-id") {
    case FileReader.Result.Error(msg) => Msg.Error(msg)
    case FileReader.Result.File(name, path, contents) => Msg.Read(contents)
  }
```

### `Http`

Please see [Networking](../networking/) for details.

### `HotReload`

If you're using a web bundler like Parcel.js to help you develop your site, then you'll know that recompiling your app with `sbt fastLinkJS` (for example) will trigger Parcel.js to automatically reload the site in your browser to show the latest changes. This is called 'hot-reloading'.

However, by default the current state of your app is not preserved between sessions, meaning you have to start from the beginning on every refresh.

The `HotReload` functionality solves this problem by saving your model to local storage periodically, and loading it up again when your app starts.

You'll need to provide a way to encode and decode your model to a `String`, and some approprite `Msg`'s but other wise, set up is as simple as adding a command to your `init` function:

```scala
HotReload.bootstrap("my-save-data", Model.decode) {
  case Left(msg)    => Msg.Log("Error during hot-reload!: " + msg)
  case Right(model) => Msg.OverwriteModel(model)
}
```

And a `Sub` to your subscriptions:

```scala
Sub.every[IO](1.second, hotReloadKey).map(_ => Msg.TakeSnapshot)
```

With an update for `Msg.TakeSnapshot` (made up `Msg` name/type),  that triggers another command:

```scala
    case Msg.TakeSnapshot =>
      (model, HotReload.snapshot(hotReloadKey, model, Model.encode))
```

### `ImageLoader`

Given a path, this cmd will load an image and create and return an `HTMLImageElement` for you to make use of.

```scala mdoc:js
import tyrian.cmds.*

val cmd: Cmd[IO, Msg] =
  ImageLoader.load("path/to/img.png") {
    case ImageLoader.Result.ImageLoadError(msg, path) => Msg.Error(msg)
    case ImageLoader.Result.Image(imageElement) => Msg.UseImage(imageElement)
  }
```

### `LocalStorage`

A series of commands that mirror the [localstorage interface](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage).

```scala mdoc:js
import tyrian.cmds.*

val cmd: Cmd[IO, Msg] =
  Cmd.Batch[IO, Msg](
    LocalStorage.setItem("key", "value") {
      case LocalStorage.Result.Success => Msg.NoOp
      case e => Msg.Error(e.toString)
    },
    LocalStorage.getItem("key") {
      case Right(LocalStorage.Result.Found(value)) => Msg.Read(value)
      case Left(LocalStorage.Result.NotFound(e)) => Msg.Error(e.toString)
    },
    LocalStorage.removeItem("key") {
      case LocalStorage.Result.Success => Msg.NoOp
      case e => Msg.Error(e.toString)
    },
    LocalStorage.clear {
      case LocalStorage.Result.Success => Msg.NoOp
      case e => Msg.Error(e.toString)
    },
    LocalStorage.key(0) {
      case LocalStorage.Result.Key(keyAtIndex0) => Msg.Read(keyAtIndex0)
      case LocalStorage.Result.NotFound(e) => Msg.Error(e.toString)
      case e => Msg.Error(e.toString)
    },
    LocalStorage.length {
      case LocalStorage.Result.Length(value) => Msg.Read(value.toString)
    }
  )
```

### `Logger`

Allows you to log to your browsers JavaScript console:

```scala mdoc:js
import tyrian.cmds.*

val cmd: Cmd[IO, Msg] =
  Logger.info("Log this!")
```

If you're app is doing a lot of regular work, you can cut down the noise with the 'once' versions:

```scala mdoc:js
import tyrian.cmds.*

val cmd: Cmd[IO, Msg] =
  Logger.debugOnce("Log this exact message only once!")
```

### `Random`

As you might expect, `Random` produces random values! Random works slightly differently from other commands, in that it doesn't except a conversion function to turn the result into a message. You do that by mapping over it.

Assuming a message `RandomValue`, here are a few examples:

```scala mdoc:js
import tyrian.cmds.*

val toMessage = (v: String) => Msg.RandomValue(v)

val cmd: Cmd[IO, Msg] =
  Cmd.Batch(
    Random.int[IO].map(i => toMessage(i.value.toString)),
    Random.shuffle[IO, Int](List(1, 2, 3)).map(l => toMessage(l.value.toString)),
    Random.Seeded(12l).alphaNumeric[IO](5).map(a => toMessage(a.value.mkString))
  )
```

## Built-in `Cmd` + `Sub` goodies

These tools make use of a combination of commands and subscriptions to achieve a result. Note that unlike in the next section, these entries share nothing apart from, say, a key value, i.e. there is no common state to manage or store in a model.

- `HotReload` - Store the state of your app, so you can carry on where you left off after a recompile!
- `Navigation` - Manage hash/anchor based browser navigation, allowing you to navigate around your app.

## Built-in Pub/Sub goodies

These entries form a pub/sub relationship where you are required to store an object that holds state in your app's model, and which allows you to then subscribe to events and publish messages via given `Sub`s and `Cmd`s respectively.

- `WebSocket` - Allows you to send and receive data to/from a socket server (see [Networking](../networking/)).
- `TyrianIndigoBridge` - Allows your Tyrian app to communicates with embedded Indigo games.
