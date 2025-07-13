package example

final case class Model(
    topNav: TopNav,
    textReverse: TextReverse,
    counters: CounterManager
)

object Model:
  val init: Model =
    Model(TopNav.initial, TextReverse.initial, CounterManager.initial)
