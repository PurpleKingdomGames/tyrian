package example

import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
    (Model.init, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Increment => (model + 1, Cmd.Empty)
      case Msg.Decrement => (model - 1, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))("-"),
      div(model.toString),
      button(onClick(Msg.Increment))("+")
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
