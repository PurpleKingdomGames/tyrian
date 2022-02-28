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
- `ImageLoader` - Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
- `LocalStorage` - Allows you to save and load to/from your browsers local storage.
- `Logger` - A simple logger that logs to the Browsers console with a few standard headers and the log message.
- `Random` - A Cmd to generate random values.

### `Dom`

Assuming two messages `Error` and `Empty`, we can attempt to focus a given ID.

```scala
import tyrian.cmds.*

val cmd: Cmd[Msg] =
  Dom.focus("my-id") {
    case Left(Dom.NotFound(id)) => Msg.Error(s"ID $id not found")
    case Right(_) => Msg.Empty
  }
```

`Dom.blur` works in the same way, though, performing the opposite effect.

### `FileReader`

Will read any file data, with build in support for text and images.

Assuming two messages `Error` and `Read`, we can attempt to read the contents of a text file.

```scala
import tyrian.cmds.*

val cmd: Cmd[Msg] =
  FileReader.readText("my-file-input-field-id") {
    case Left(FileReader.Error(msg)) => Msg.Error(msg)
    case Right(FileReader.File(name, path, contents)) => Msg.Read(contents)
  }
```

### `Http`

Please see the [http example](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples) for details.

### `ImageLoader`

Given a path, this cmd will load an image and create and return an `HTMLImageElement` for you to make use of.

```scala
import tyrian.cmds.*

val cmd: Cmd[Msg] =
  ImageLoader.load("path/to/img.png") {
    case Left(ImageLoader.ImageLoadError(msg, path)) => Msg.Error(msg)
    case Right(imageElement) => Msg.UseImage(imageElement)
  }
```

### `LocalStorage`

A series of commands that mirror the [localstorage interface](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage).

```scala
import tyrian.cmds.*

val cmd: Cmd[Msg] =
  Cmd.Batch(
    LocalStorage.setItem("key", "value") {
      case Right(_) => Msg.NoOp
      case Left(e) => Msg.Error(e.toString)
    },
    LocalStorage.getItem("key") {
      case Right(value) => Msg.Read(value)
      case Left(e) => Msg.Error(e.toString)
    },
    LocalStorage.removeItem("key") {
      case Right(_) => Msg.NoOp
      case Left(e) => Msg.Error(e.toString)
    },
    LocalStorage.clear {
      case Right(_) => Msg.NoOp
      case Left(e) => Msg.Error(e.toString)
    },
    LocalStorage.key(0) {
      case Right(keyAtIndex0) => Msg.Read(keyAtIndex0)
      case Left(e) => Msg.Error(e.toString)
    },
    LocalStorage.length {
      case Right(value) => Msg.Read(value)
      case Left(e) => Msg.Error(e.toString)
    }
  )
```

### `Logger`

Allows you to log to your browsers JavaScript console:

```scala
import tyrian.cmds.*

val cmd: Cmd[Msg] =
  Logger.info("Log this!")
```

If you're app is doing a lot of regular work, you can cut down the noise with the 'once' versions:

```scala
import tyrian.cmds.*

val cmd: Cmd[Msg] =
  Logger.debugOnce("Log this exact message only once!")
```

### `Random`

As you might expect, `Random` produces random values! Random works slightly differently from other commands, in that it doesn't except a conversion function to turn the result into a message. You do that by mapping over it.

Assuming a message `RandomValue`, here are a few examples:

```scala
import tyrian.cmds.*

def toMessage = (v: String) => Msg.RandomValue(v.toString)

val cmd: Cmd[Msg] =
  Cmd.Batch(
    Random.int.map(toMessage(_.toString)),
    Random.shuffle(List(1, 2, 3)).map(toMessage(_.toString)),
    Random.Seeded(12l).alphaNumeric(5).map(toMessage)
  )
```

## Built-in `Cmd` + `Sub` goodies

These tools make use of a combination of commands and subscriptions to achieve a result. Note that unlike in the next section, these entries share nothing apart from, say, a key value, i.e. there is no common state to manage or store in a model.

- `HotReload` - Store the state of your app, so you can carry on where you left off after a recompile!
- `Navigation` - Manage hash/anchor based browser navigation, allowing you to navigate around your app.

## Built-in Pub/Sub goodies

These entries form a pub/sub relationship where you are required to store an object that holds state in your app's model, and which allows you to then subscribe to events and publish messages via given `Sub`s and `Cmd`s respectively.

- `WebSocket` - Allows you to send and receive data to/from a socket server.
- `TyrianIndigoBridge` - Allows your Tyrian app to communicates with embedded Indigo games.
