package example

import tyrian.{Html, Tyrian}
import tyrian.Html._
import org.scalajs.dom.document

object Main:

  opaque type Model = List[Counter.Model]

  enum Msg:
    case Insert                           extends Msg
    case Remove                           extends Msg
    case Modify(i: Int, msg: Counter.Msg) extends Msg

  def init: Model =
    Nil

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Insert =>
        Counter.init :: model

      case Msg.Remove =>
        model match
          case Nil    => Nil
          case _ :: t => t

      case Msg.Modify(id, m) =>
        model.zipWithIndex.map { case (c, i) =>
          if i == id then Counter.update(m, c) else c
        }

  def view(model: Model): Html[Msg] =
    val counters = model.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div()(elems: _*)

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view)

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
