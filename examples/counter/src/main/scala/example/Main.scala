package example

import org.scalajs.dom.Element
import org.scalajs.dom.document
import tyrian.Html._
import tyrian._

object Main extends TyrianApp[Msg, Model]:

  def container: Element = document.getElementById("myapp")

  def init: (Model, Cmd[Msg]) = (Model.init, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Increment => (model + 1, Cmd.Empty)
      case Msg.Decrement => (model - 1, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    div()(
      button(onClick(Msg.Decrement))(text("-")),
      div()(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

opaque type Model = Int
object Model:
  def init: Model = 0

  extension (i: Model)
    def +(other: Int): Model = i + other
    def -(other: Int): Model = i - other

enum Msg:
  case Increment, Decrement
