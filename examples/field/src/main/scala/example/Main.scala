package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) = ("", Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NewContent(content) => (content, Cmd.None)

  def view(model: Model): Html[Msg] =
    div()(
      input(
        placeholder := "Text to reverse",
        onInput(s => Msg.NewContent(s)),
        myStyle
      ),
      div(myStyle)(text(model.reverse))
    )

  private val myStyle =
    styles(
      "width"      -> "100%",
      "height"     -> "40px",
      "padding"    -> "10px 0",
      "font-size"  -> "2em",
      "text-align" -> "center"
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

type Model = String

enum Msg:
  case NewContent(content: String) extends Msg
