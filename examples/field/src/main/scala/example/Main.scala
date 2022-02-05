package example

import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) = ("", Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.NewContent(content) => (content, Cmd.Empty)

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

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

type Model = String

enum Msg:
  case NewContent(content: String) extends Msg
