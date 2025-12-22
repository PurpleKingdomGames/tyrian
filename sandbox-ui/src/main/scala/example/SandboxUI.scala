package example

import tyrian.*
import tyrian.next.*
import tyrian.ui.theme.Theme

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object SandboxUI extends TyrianNext[Model]:

  given Theme = Theme.default

  def router: Location => GlobalMsg =
    Routing.externalOnly(AppEvent.NoOp, AppEvent.FollowLink(_))

  def init(flags: Map[String, String]): Outcome[Model] =
    Outcome(Model.init)

  def update(model: Model): GlobalMsg => Outcome[Model] =
    case e =>
      model.update(e)

  def view(model: Model): HtmlRoot =
    HtmlRoot(model.view)

  def watchers(model: Model): Batch[Watcher] =
    Batch.empty
