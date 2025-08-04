package example

import cats.effect.IO
import tyrian.*
import tyrian.ui.*

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
        TextBlock("Welcome to Tyrian UI!").toHeading1
          .withColor(RGBA.fromHexString("#2563eb")),
        Row(
          TextBlock("Hello, Tyrian!").withColor(RGBA.Blue),
          Button(Msg.NoOp).withLabel("Click me!"),
          TextBlock("More text").withColor(RGBA.Red.mix(RGBA.Blue))
        )
          .withSpacing(Spacing.Medium)
          .center,
        TextBlock("This is just some text")
          .withColor(RGBA.fromHexString("#6b7280"))
      ),
      Column(
        Container(
          TextBlock("This is some more text")
        )
      )
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
