package example

import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
    (Nil, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Insert =>
        (Counter.init :: model, Cmd.Empty)

      case Msg.Remove =>
        model.toList match
          case Nil    => (Nil, Cmd.Empty)
          case _ :: t => (t, Cmd.Empty)

      case Msg.Modify(id, m) =>
        val updated = model.toList.zipWithIndex.map { case (c, i) =>
          if i == id then Counter.update(m, c) else c
        }

        (updated, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    val counters = model.toList.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div()(elems: _*)

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

type Model = List[Counter.Model]

enum Msg:
  case Insert                           extends Msg
  case Remove                           extends Msg
  case Modify(i: Int, msg: Counter.Msg) extends Msg

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg:
    case Increment, Decrement

  def view(model: Model): Html[Msg] =
    div()(
      button(onClick(Msg.Decrement))(text("-")),
      div()(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1
