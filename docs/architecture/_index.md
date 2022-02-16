+++
title = "Architecture & Patterns"
menuTitle = "Architecture"
weight = 2
+++

## The Elm Architecture

Tyrian provides a runtime environment for executing applications that was originally designed according to the [Elm architecture](https://guide.elm-lang.org/architecture/).

Elm is the name of a language and an ecosystem, but it's architecture has become more widely known as the 'TEA Pattern' (_(**T**)he (**E**)lm (**A**)rchitecture_) and has influenced many GUI/UI libraries and implementations beyond the world of functional programming.

### The TEA pattern

The TEA Pattern is about:

1. Immutable data.
2. Pure functions.
3. Uni-directional data flow.
4. Strictly ordered events and updates.

This gives you a system that is very easy it reason about, since the data cannot (or is unlikely to) be subject to hard-to-test race conditions or side effects, and everything happens in a predictable order.

The purity of the system, the way that the state is held apart from the processing and rendering functions, also allows for easy testing without the need for complex mocking.

In essence:

- The state of the application is modeled by an immutable `Model`.
- Events that change the state of the application are modeled by an immutable `Msg` type.
- State transitions are implemented by a `(Msg, Model) => Model` function.
- Finally, a `Model => Html[Msg]` function defines how to render the state of the application in HTML.

## Tyrian vs Elm

As you might expect, Elm is a far richer offering that Tyrian is now, and in all likelihood ever will be. It's been around a lot longer, and there are a lot more people working on and with Elm every day feeding into it's design.

Having said that: While Elm's architecture has taken on a life of it's own and influenced the state of the art of functional (and even non-FP!) UI programming, Elm itself has remained somewhat niche.

People tend to love Elm ...or hate it. A lot of that reaction can be attributed to the fact that Elm is a very opinionated language, and you either like that or you do not.

What Elm's opinionated stance buys you is an ecosystem where if your code compiles, it works! And that is an amazing thing! In the cost/benefit analysis: The benefit is an incredibly robust web development experience, at the cost of literally not being able to do anything that is not permitted (because it would break the robustness guarantees of the ecosystem).

Tyrian takes the glorious essence of Elm's architecture, but removes almost all of the opinions ...and with it of course, almost all of the safety nets!

- Want to break up your code into lots of files and classes? Carry on.
- Want to use refined types? Ok then.
- Want to use Typeclasses? No problem.
- Want to bring in a heavyweight FP library? Sure thing.
- Want to talk to JavaScript directly over an FFI? Go nuts.
- Want to do a non-exhaustive match? I mean, you can but...
- Want to throw a massive exception? ...erm ...sure...

Be safe out there folks! 😀

## Tyrian & Indigo

Tyrian and [Indigo](https://indigoengine.io/) are siblings and both follow an interpretation of the TEA Pattern, but they grew up at different times under different influences, and so they are not exactly the same in their design.

Tyrian is designed in the same image as Elm, and for the same purpose: Building rich web apps. Indigo however is a game engine. They do have a lot of things in common:

- The APIs are all pure functions
- The state is immutable
- The data follows a uni-directional path
- Events/Messages are strictly ordered
- ...even the names and signatures of the API functions are similar

...and so on.

But if you consider the life of a GUI app versus a game - in general - user interfaces don't do anything except as a reaction to user input. Yes, there are exceptions, you can do animations, people do use Elm to make games, etc. But as a broad brush principle, most of the time your word processor or photo editing applications are doing nothing unless you are doing something with them. Hammering keys and painting with you mouse.

Games on the other hand are normally doing things _all the time_. Even if the player isn't doing anything! There _will_ be background animations, particle effects will be firing, non-player characters will be walking around, your character will get bored and start impatiently tapping their foot.

Two of the main ways this difference visibly manifests itself are:

1. `Cmd` vs `Outcome`
2. The nature of your `Model` instance

### `Cmd` vs `Outcome`

The update function in Tyrian returns a `(Model, Cmd)` where the command is a lazily evaluated task - perhaps an HTTP call or some other side effect. Whereas in Indigo you return an `Outcome`, but an outcome does not allow you to directly describe actions that would need evaluation, it only captures updated values and events. If you want to make an HTTP call, there's an event for that.

In Tyrian we expect that you'll be doing a lot of out-of-band/concurrent/side-effecting work like calling web services and interacting with JavaScript, in Indigo we assume you mostly won't be.

This has the interesting side effect that Tyrian's update functions are referentially transparent, but Indigo's are both referentially transparent and declarative. The former allows you to do more practical things, the latter is lighter and easier to test, but less clean if you do need to, say, call down to JavaScript.

### The nature of your `Model`

What is in your model?

In a GUI app, your model is probably quite close to representing the things that will be displayed. Perhaps it holds the current state of a quiz or survey for example, or the items in a todo list.

In an Indigo game, the model tends to be much more abstract and divorced from any presentation concerns. So much so that Indigo has an extra model called a `ViewModel` that behaves more like a model in Tyrian.

As an example: Consider the score counter on a pinball machine. What makes these fun is that they do not simply show your score, they rapidly roll through the numbers always trying to _keep up_ with your score!

We have two things we have to know to draw this effect:

1. The real score the player has achieved.
2. The score currently being shown.

In Tyrian, both of these values would be held in the `Model`.
In Indigo, the real score (1) would be held in the `Model` as it's a factual piece of data, while the currently displayed score would live in the `ViewModel` since it's purely there for presentation purposes.
