package example

import scalm.{Html, Scalm}
import scalm.Html._
import org.scalajs.dom.document

object Main {

  // MODEL

  type Model = List[Counter.Model]

  sealed trait Msg
  case object Insert extends Msg
  case object Remove extends Msg
  final case class Modify(i: Int, msg: Counter.Msg) extends Msg

  def init: Model = Nil

  // VIEW

  def view(model: Model): Html[Msg] = {
    val counters = model.zipWithIndex.map {
      case (c, i) =>
        Counter.view(c).map(msg => Modify(i, msg))
    }

    val elems = List(
      button(onClick(Remove))(text("remove")),
      button(onClick(Insert))(text("insert"))
    ) ++ counters

    div()(elems:_*)
  }

  // UPDATE

  def update(msg: Msg, model: Model): Model =
    msg match {
      case Insert =>
        Counter.init :: model
      case Remove =>
        model match {
          case Nil => Nil
          case _ :: t => t
        }
      case Modify(id, m) =>
        model.zipWithIndex.map { case (c, i) =>
            if (i == id) {
              Counter.update(m, c)
            } else {
              c
            }
        }
    }

  def main(args: Array[String]): Unit = Scalm.start(document.body)(init, update, view)
}

object Counter {

  // MODEL

  type Model = Int

  def init: Model = 0

  sealed trait Msg
  case object Increment extends Msg
  case object Decrement extends Msg

  // VIEW

  def view(model: Model): Html[Msg] =
    div()(
      button(onClick(Decrement))(text("-")),
      div()(text(model.toString)),
      button(onClick(Increment))(text("+"))
    )

  // UPDATE

  def update(msg: Msg, model: Model): Model =
    msg match {
      case Increment => model + 1
      case Decrement => model - 1
    }
}
