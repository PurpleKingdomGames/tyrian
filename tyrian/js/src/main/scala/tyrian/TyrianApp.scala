package tyrian

import org.scalajs.dom.document

import scala.scalajs.js.annotation._

trait TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg])

  def update(msg: Msg, model: Model): (Model, Cmd[Msg])

  def view(model: Model): Html[Msg]

  def subscriptions(model: Model): Sub[Msg]

  @JSExport
  def launch(containerId: String): Unit =
    ready(containerId, Map[String, String]())

  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(containerId, flags.toMap)

  private def ready(parentElementId: String, flags: Map[String, String]): Unit =
    Tyrian.start(document.getElementById(parentElementId), init(flags), update, view, subscriptions)
