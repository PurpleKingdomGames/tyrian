package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Nil, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Insert =>
      (Counter.init :: model, Cmd.None)

    case Msg.Remove =>
      model.toList match
        case Nil    => (Nil, Cmd.None)
        case _ :: t => (t, Cmd.None)

    case Msg.Modify(id, m) =>
      val updated = model.toList.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      (updated, Cmd.None)

    case Msg.NoOp =>
      (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    val counters = model.toList.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div()(elems*)

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

type Model = List[Counter.Model]

enum Msg:
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case NoOp

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
