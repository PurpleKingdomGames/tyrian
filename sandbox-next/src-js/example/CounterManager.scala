package example

import tyrian.Html.*
import tyrian.next.*

final case class CounterManager(counters: List[Counter]):

  def update: GlobalMsg => Outcome[CounterManager] =
    case CounterManagerEvent.Modify(index, msg) =>
      val cs = counters.zipWithIndex.map { case (c, i) =>
        if i == index then c.update(msg) else c
      }

      Outcome(this.copy(counters = cs))

    case CounterManagerEvent.Insert =>
      Outcome(
        this.copy(
          counters = Counter.initial :: counters
        )
      )

    case CounterManagerEvent.Remove =>
      Outcome(this.copy(counters = counters.drop(1)))

    case _ =>
      Outcome(this)

  def view: HtmlFragment =
    HtmlFragment.insert(
      MarkerIds.counters,
      div(
        List(
          button(onClick(CounterManagerEvent.Remove))(text("remove")),
          button(onClick(CounterManagerEvent.Insert))(text("insert"))
        ) ++
          counters.zipWithIndex.map { case (c, i) =>
            c.view.map(msg => CounterManagerEvent.Modify(i, msg))
          }
      )
    )

object CounterManager:
  val initial: CounterManager =
    CounterManager(Nil)

enum CounterManagerEvent extends GlobalMsg:
  case Modify(index: Int, msg: CounterEvent)
  case Insert
  case Remove
