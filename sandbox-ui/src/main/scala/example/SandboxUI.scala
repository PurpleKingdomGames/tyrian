package example

import cats.effect.IO
import tyrian.*
import tyrian.ui.*
import tyrian.ui.button.Button
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing
import tyrian.ui.layout.Column
import tyrian.ui.layout.Row
import tyrian.ui.text.Text

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object SandboxUI extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg =
    Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp =>
      (model, Cmd.None)

  given Theme = Theme.default

  def view(model: Model): Html[Msg] =
    Row(
      Column(
        Text("Welcome to Tyrian UI!").toHeading1
          .withColor(RGBA.fromHexString("#2563eb")),
        Row(
          Text("Hello, Tyrian!").withColor(RGBA.Blue),
          Button(Msg.NoOp).withLabel("Click me!"),
          Text("More text").withColor(RGBA.Red.mix(RGBA.Blue))
        )
          .withSpacing(Spacing.Medium)
          .center,
        Text("This is just some text")
          .withColor(RGBA.fromHexString("#6b7280"))
      ),
      Column(Text("This is some more text"))
    )
      .withSpacing(Spacing.Large)
      .center
      .middle
      .toHtml

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

enum Msg:
  case NoOp

final case class Model()

object Model:
  val init: Model =
    Model()
