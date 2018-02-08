package example

import org.scalajs.dom.document
import scalm.Html._
import scalm.{Html, Scalm, Style}

object Main {
  def main(args: Array[String]): Unit =
    Scalm.start(document.body)(init, update, view)

  type Model = String

  def init: Model = ""

  sealed trait Msg
  case class NewContent(content: String) extends Msg

  def update(msg: Msg, model: Model): Model =
    msg match {
      case NewContent(content) => content
    }

  def view(model: Model): Html[Msg] =
    div()(
      input(placeholder("Text to reverse"), onInput(NewContent), myStyle),
      div(myStyle)(text(model.reverse))
    )

  private val myStyle =
    style(
      Style("width", "100%"),
      Style("height", "40px"),
      Style("padding", "10px 0"),
      Style("font-size", "2em"),
      Style("text-align", "center")
    )
}
