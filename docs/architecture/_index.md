+++
title = "Architecture"
menuTitle = "Architecture & Patterns"
weight = 2
+++

### The Elm Architecture (TEA Pattern)

Tyrian provides a runtime environment for executing applications designed
according to the [Elm architecture](https://guide.elm-lang.org/architecture/).

In essence, the state of the application is modeled by an immutable `Model`,
events that change the state of the application are modeled by an immutable
`Msg` type, state transitions are implemented by a `(Msg, Model) => Model`
function, and finally, a `Model => Html[Msg]` function defines how to render
the state of the application in HTML.

### Tyrian vs Indigo

// TODO
