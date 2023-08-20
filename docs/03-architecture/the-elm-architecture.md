# The Elm Architecture

Tyrian provides a runtime environment for executing applications that was originally designed according to the [Elm architecture](https://guide.elm-lang.org/architecture/).

Elm is the name of a language and an ecosystem, but it's architecture has become more widely known as the 'TEA Pattern' (_(**T**)he (**E**)lm (**A**)rchitecture_) and has influenced many GUI/UI libraries and implementations beyond the world of functional programming.

## The TEA pattern

The TEA Pattern is about:

1. Immutable data.
2. Pure functions.
3. Uni-directional data flow.
4. Strictly ordered events and updates.

This gives you a system that is very easy to reason about, since the data cannot (or is unlikely to) be subject to hard-to-test race conditions or side effects, and everything happens in a predictable order.

The purity of the system, the way that the state is held apart from the processing and rendering functions, also allows for easy testing without the need for complex mocking.

In essence:

- The state of the application is modeled by an immutable `Model`.
- Events that change the state of the application are modeled by an immutable `Msg` type.
- State transitions are implemented by a `(Msg, Model) => Model` function.
- Finally, a `Model => Html[Msg]` function defines how to render the state of the application in HTML.
