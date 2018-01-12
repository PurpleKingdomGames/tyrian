package example

import scalm.{Html, Scalm}
import scalm.Html._
import org.scalajs.dom.document

object Main {
  def main(args: Array[String]): Unit = Scalm.start(document.body)(init, update, view)

  type Model = Int

  def init: Model = 0

  sealed trait Msg
  case object Increment extends Msg
  case object Decrement extends Msg

  def update(msg: Msg, model: Model): Model =
    msg match {
      case Increment => model + 1
      case Decrement => model - 1
    }

  def view(model: Model): Html[Msg] =
    div()(
      button(onClick(Decrement))(text("-")),
      div()(text(model.toString)),
      button(onClick(Increment))(text("+"))
    )
}