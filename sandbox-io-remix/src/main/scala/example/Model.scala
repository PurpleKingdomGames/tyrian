package example

import cats.effect.IO
import tyrian.*
import tyrian.Html.*

final case class Model(
    topNav: TopNav,
    textReverse: TextReverse,
    counters: CounterManager
):

  def update: GlobalMsg => Outcome[Model] =
    case AppEvent.NoOp =>
      Outcome(this)

    case AppEvent.FollowLink(href) =>
      Outcome(this)
        .addActions(Nav.loadUrl[IO](href))

    case e =>
      for {
        tn <- topNav.update(e)
        tr <- textReverse.update(e)
        cs <- counters.update(e)
      } yield this.copy(
        topNav = tn,
        textReverse = tr,
        counters = cs
      )

  def view: Html[GlobalMsg] =
    div(
      topNav.view,
      textReverse.view,
      counters.view
    )

object Model:
  val init: Model =
    Model(TopNav.initial, TextReverse.initial, CounterManager.initial)
