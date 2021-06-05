package example

import tyrian.{Html, Tyrian}
import tyrian.Html._
import org.scalajs.dom.document

object Main:
  opaque type Model = Int

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view)

  def init: Model = 0

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1

  def view(model: Model): Html[Msg] =
    // body()(
    div(`class`("container"))(
      div(`class`("row"))(
        div(`class`("col bodyText"), styles("text-align" -> "right"))(button(onClick(Msg.Decrement))(text("-"))),
        div(`class`("col bodyText"), styles("text-align" -> "center"))(text(model.toString)),
        div(`class`("col bodyText"), styles("text-align" -> "left"))(button(onClick(Msg.Increment))(text("+")))
      )
    )
// )

enum Msg:
  case Increment, Decrement
