package example

import org.scalajs.dom.document
import scalm.Html._
import scalm.{Html, Scalm, Style}

object Main {
  def main(args: Array[String]): Unit =
    Scalm.start(document.body, init, update, view)

  type Model = String

  def init: Model = ""

  enum Msg:
    case NewContent(content: String) extends Msg

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.NewContent(content) => content

  def view(model: Model): Html[Msg] =
    div()(
      input(placeholder("Text to reverse"), onInput(s => Msg.NewContent(s)), myStyle),
      div(myStyle)(text(model.reverse))
    )

  private val myStyle =
    style(
      "width" -> "100%",
      "height" -> "40px",
      "padding" -> "10px 0",
      "font-size" -> "2em",
      "text-align" -> "center"
    )
}
