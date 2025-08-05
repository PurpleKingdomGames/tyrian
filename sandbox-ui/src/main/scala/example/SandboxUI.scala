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
          .withColor(RGBA.fromHex("#2563eb")),
        Row(
          TextBlock("Hello, Tyrian!").withColor(RGBA.Blue),
          Button(Msg.NoOp).withLabel("Click me!"),
          TextBlock("More text").withColor(RGBA.Red.mix(RGBA.Blue))
        )
          .withSpacing(Spacing.Medium),
        TextBlock("This is just some text")
          .withColor(RGBA.fromHex("#6b7280")),
        tyrian.ui.html.HtmlElement(
          tyrian.Html.div(
            tyrian.Html.style := "border: 2px dashed #ccc; padding: 1rem; border-radius: 4px; margin: 1rem 0;"
          )(
            tyrian.Html.p("This is arbitrary HTML embedded within the UI component system!"),
            tyrian.Html.strong("Bold text"),
            tyrian.Html.text(" and "),
            tyrian.Html.em("italic text")
          )
        )
      ),
      Column(
        Container(
          TextBlock("This is some more text.")
        ).middle.center,
        Image(
          "https://raw.githubusercontent.com/PurpleKingdomGames/roguelike-starterkit/417f4e372b4792972ef62aea0c917088a9fc82fd/roguelike.gif",
          "Roguelike"
        )
          .withSize("200px", "150px")
          .cover
          .rounded // TODO: This isn't working right. The rounded isn't being preseved when the solid border is applied.
          .withSolidBorder(BorderWidth.Medium, RGBA.fromHex("#2563eb"))
      )
    )
      .withSpacing(Spacing.Large)
      .toHtml

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

enum Msg:
  case NoOp

final case class Model()

object Model:
  val init: Model =
    Model()
