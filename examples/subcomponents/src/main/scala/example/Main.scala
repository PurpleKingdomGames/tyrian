package example

import org.scalajs.dom.document
import tyrian.Html._
import tyrian._

object Main:

  opaque type Model = List[Counter.Model]

  enum Msg:
    case Insert                           extends Msg
    case Remove                           extends Msg
    case Modify(i: Int, msg: Counter.Msg) extends Msg

  def init: (Model, Cmd[Msg]) =
    (Nil, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Insert =>
        (Counter.init :: model, Cmd.Empty)

      case Msg.Remove =>
        model match
          case Nil    => (Nil, Cmd.Empty)
          case _ :: t => (t, Cmd.Empty)

      case Msg.Modify(id, m) =>
        val updated = model.zipWithIndex.map { case (c, i) =>
          if i == id then Counter.update(m, c) else c
        }

        (updated, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    val counters = model.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div()(elems: _*)

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
