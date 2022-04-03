package tyrian

import cats.effect.unsafe.implicits.global
import org.scalajs.dom.document
import tyrian.runtime.RunWithCallback

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

  def launch(containerId: String, flags: Map[String, String]): Unit =
    ready(containerId, flags)

  private def ready(parentElementId: String, flags: Map[String, String]): Unit =
    val runner: RunWithCallback[Msg] = task => cb => task.unsafeRunAsync(cb)
    Tyrian.start(
      document.getElementById(parentElementId),
      init(flags),
      update,
      view,
      subscriptions,
      runner
    )
