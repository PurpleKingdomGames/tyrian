package example

import tyrian.*
import tyrian.Html.*
import tyrian.next.*

final case class Counter(value: Int):
  def update: CounterEvent => Counter =
    case CounterEvent.Increment =>
      this.copy(value = value + 1)

    case CounterEvent.Decrement =>
      this.copy(value = value - 1)

  def view: Html[CounterEvent] =
    div(
      button(onClick(CounterEvent.Decrement))(text("-")),
      div(text(value.toString)),
      button(onClick(CounterEvent.Increment))(text("+"))
    )

object Counter:

  val initial: Counter =
    Counter(0)

enum CounterEvent extends GlobalMsg:
  case Increment
  case Decrement
