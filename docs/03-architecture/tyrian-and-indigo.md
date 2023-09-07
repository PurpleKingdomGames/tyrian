# Tyrian & Indigo

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
