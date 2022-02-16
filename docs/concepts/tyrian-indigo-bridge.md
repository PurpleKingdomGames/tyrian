---
title: "Tyrian-Indigo Bridge"
menuTitle: "Tyrian + Indigo "
---

What if you wanted to make a web game (or an Electron desktop game - why not!), where the main game window was rendered in WebGL, but the UI elements were all in lovely, responsive, scalable HTML5?

The main problem is how you get the two parts to talk to one another. If everything is written in JavaScript, that's not such a problem because it's all the same language, the two systems are likely to be idiomatically similar, and with JavaScript ...well, you can do pretty much anything you like.

If you want to use Scala.js, things get more complicated.

## Tyrian 💜's Indigo

One of the reasons Tyrian was resurrected from the ashes of it's predecessor [Scalm](https://github.com/julienrf/scalm) was that we love working with this architecture pattern, the TEA pattern. [Indigo](https://indigoengine.io/) our game engine, uses a variation on the same design.

Both use the same language... both use the same idioms... why not join them together?

## Runnable Example

Rather than go into all the details of how it works, there is an [example](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples) you can look at the demonstrates how the two can seamlessly communicate using lovely native Scala types.

> Note that the example is quite simple and just uses `String` as it's message format, but you can use whatever suits your needs.

## Overview making Tyrian work with Indigo

Assuming you have a skeletal Tyrian project, and right next to it in the same code base a basic Indigo game (Tip: namespacing the game to its own package is a good idea... you're about to have two models!):

### The Bridge

The way the bridge works is that the Tyrian side holds an instance of the bridge in it's model, and Indigo runs a subsystem for that bridge.

Indigo SubSystems are like mini-games that have most of the same functions as the main game, but can only talk via messages - sort of like background workers.

The Bridge is a JavaScript `EventTarget`, and both Indigo and Tyrian latch on. In both instances their job is to send and receive messages via the brigde event target and convert them to their native event/messaging system.

### Establishing a connection

1. Indigo needs a container to latch onto, so Tyrian provides that in it's rendered view.
1. During `init`, you start up the bridge and put it in your model, then use a `Cmd.Emit` to tell the `updateModel` function to start the game.
1. An Indigo game can then be trivially launched using a `Cmd.SideEffect` that literally calls your game's launch function in plain ole Scala, injecting the bridge provided SubSystem into the game.

***Important:*** The key tools you need are now all on your bridge instance. Your bridge instance will give you the Indigo `SubSystem` for you, it also has the `publish` `Cmd` and the `subscribe` `Sub` that you need.

### Publishing and Subscribing 

Note that Indigo uses the term 'event' and Tyrian uses 'message'. The behavior is slightly different between the two, but for our purposes here, they should be considered the same thing.

#### In Indigo

Indigo now has a new `GlobalEvent` type, `TyrianEvent` which can be a `TyrianEvent.Send(value: A)` or `TyrianEvent.Receive(value: A)` where the `A` type is declared with the `SubSystem`.

To listen for messages from Tyrian, you simply match on the `TyrianEvent.Receive(value: A)` in one of you update functions.

To send a message to Tyrian, you emit a message as normal, e.g.:

```scala
Outcome(..).addGlobalEvents(TyrianEvent.Send(myMessage))
```

#### In Tyrian

Tyrian by now has a reference to the bridge in your model.

To listen for messages from Indigo, all you have to do is plumb in the `model.bridge.subscribe` `Sub[Msg]` into your app's `subscriptions` feed, and provide an extractor to tell it how to convert the events into messages.

To send message to Indigo, you call `bridge.send(myMsg)` which provides a `Cmd[Msg]` for you to plug into your `updateModel` result.

#### That's it!

In a nutshell, that is the whole set up. Happy web game building!
