package example

import tyrian.*
import tyrian.Html.*

import scala.scalajs.js.annotation.*

// TODO: Think about 'out of order' rendering: HtmlFragment?

@JSExportTopLevel("TyrianApp")
object SandboxIORemix extends TyrianIO[Model]:

  def router: Location => GlobalMsg = Routing.externalOnly(AppEvent.NoOp, AppEvent.FollowLink(_))

  def init(flags: Map[String, String]): Outcome[Model] =
    Outcome(Model.init)

  def update(model: Model): GlobalMsg => Outcome[Model] =
    case e: CounterManagerEvent =>
      model.counters.update(e).map { updated =>
        model.copy(counters = updated)
      }

    case e: TextReverseEvent =>
      model.textReverse.update(e).map { updated =>
        model.copy(textReverse = updated)
      }

    case e: AppEvent =>
      handleAppEvent(model)(e)

    case _ =>
      Outcome(model)

  def handleAppEvent(model: Model): AppEvent => Outcome[Model] =
    case AppEvent.NoOp =>
      Outcome(model)

    case AppEvent.FollowLink(href) =>
      Outcome(model)
        .addActions(
          Action.fromCmd(Nav.loadUrl(href))
        ) // TODO: Will need Nav equivalents. OR, we just accept Cmds in the addActions, or make a new addCmds thing.

  def view(model: Model): Html[GlobalMsg] =
    div(
      model.topNav.view,
      model.textReverse.view,
      model.counters.view
    )

  def watchers(model: Model): Watch =
    Watch.None
