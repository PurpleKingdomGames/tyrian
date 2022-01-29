package example

import org.scalajs.dom.document
import tyrian.Html._
import tyrian._

object Main:

  type Model = String

  def init: (Model, Cmd[Msg]) = ("", Cmd.Empty)

  enum Msg:
    case NewContent(content: String) extends Msg

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

  def main(args: Array[String]): Unit =
    Tyrian.start(
      document.getElementById("myapp"),
      init,
      update,
      view,
      subscriptions
    )
