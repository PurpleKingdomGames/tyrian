---
title: "Commands & Effects"
menuTitle: "Commands"
---

## Commands

Using Tyrian, you can get a long way with nothing more than a model, a view, and some messages being pumped around their endless one-way circuit.

However, sooner or later you are going to want to do something that appears to break the loop, for instance:

- Make an HTTP call
- Log a message to the console
- Invoke a download
- Draw to a canvas

These kinds of actions are called 'side-effects', and are an extremely popular talking point with functional programmers, because they represent the very moment that all their beautiful functional purity goes right out the window.

Luckily, Tyrian has a pretty elegant solution for this.

### Monadic effect handling

> You can skip this bit of context if you like. This section has a bit of jargon in it for those who care about such things, but it's really not important if you just want to know how to use commands.

The usual approach to handling effects (short for side effects) is to employ some sort of effect monad that captures your side effect as a lazy value.

This is _exactly_ what a command (`Cmd`) does. While commands themselves are only Monoidal Functors*, they work using a Monadic `Task` implementation under the covers.

(* Meaning you can `map` over them, they can be combined together, and have an empty state `Cmd.empty`.)

So far, this is sounding like effect handling as usual. But no. Primarily because you never (or rarely) actually see the underlying `Task`.

### Making things happen

Commands take the form `Cmd[Msg]` which is to say that they represent some sort of side effect that can produce a message to be cleanly fed back into your single page application's `updateModel` function.

Commands can be produced as part of a result of calling the `init` or `updateModel` functions, which both return a `(Model, Cmd[Msg])`.

Here is an example in which, on receiving a message `Msg.LogThis`, we are not going to change the model, but we want to write to the browser's JavaScript console:

```scala mdoc:silent
import tyrian.*
import tyrian.cmds.*

type Model = Int

enum Msg:
  case LogThis(message: String)

def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
  msg match
    case Msg.LogThis(msg) =>
      (model, Logger.consoleLog(msg))
```

To achieve this, we use the `Logger` command that comes with Tyrian. The `Logger` command is in fact just a `Cmd.SideEffect` that captures a value or behavior as a zero argument function, known as a `thunk`, in this case a simplified implementation could just be:

```scala mdoc:silent
def consoleLog(msg: String): Cmd[Nothing] =
  Cmd.SideEffect { () =>
    println(msg)
  }
```

But commands can also return values in the form of messages. The `Random` command looks like this:

```scala mdoc:silent
Random.double
```

...and produces an instance of `RandomValue`, but this leads to a problem since `RandomValue` is almost certainly not your app's `Msg` type, and so we must map over the result:

```scala mdoc:silent
enum MyMsg:
  case MyRandom(d: Double) extends MyMsg
  case Error               extends MyMsg

Random.double.map {
  case RandomValue.NextDouble(d) => MyMsg.MyRandom(d)
  case _                         => MyMsg.Error
}
```

These are simple examples, but there are much more complicated uses for commands. One great use of commands is for making [HTTP requests where the response is decoded into a `Msg`.](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples)
