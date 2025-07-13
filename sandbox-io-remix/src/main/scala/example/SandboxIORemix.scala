package example

import tyrian.*

import scala.scalajs.js.annotation.*

// TODO: Think about 'out of order' rendering: HtmlFragment?

@JSExportTopLevel("TyrianApp")
object SandboxIORemix extends TyrianIO[Model]:

  def router: Location => GlobalMsg =
    Routing.externalOnly(AppEvent.NoOp, AppEvent.FollowLink(_))

  def init(flags: Map[String, String]): Outcome[Model] =
    Outcome(Model.init)

  def update(model: Model): GlobalMsg => Outcome[Model] =
    case e =>
      model.update(e)

  def view(model: Model): Html[GlobalMsg] =
    model.view

  def watchers(model: Model): Watch =
    Watch.None
