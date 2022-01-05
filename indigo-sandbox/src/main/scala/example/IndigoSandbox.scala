package example

import tyrian.{Html, Tyrian}
import tyrian.Html._
import org.scalajs.dom.document

object IndigoSandbox:

  enum Msg:
    case NewContent(content: String)      extends Msg
    case Insert                           extends Msg
    case Remove                           extends Msg
    case Modify(i: Int, msg: Counter.Msg) extends Msg

  def init: Model =
    Model.init

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.NewContent(content) =>
        model.copy(field = content)

      case Msg.Insert =>
        model.copy(components = Counter.init :: model.components)

      case Msg.Remove =>
        val cs = model.components match
          case Nil    => Nil
          case _ :: t => t

        model.copy(components = cs)

      case Msg.Modify(id, m) =>
        val cs = model.components.zipWithIndex.map { case (c, i) =>
          if i == id then Counter.update(m, c) else c
        }

        model.copy(components = cs)

  def view(model: Model): Html[Msg] =
    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div(
      div(
        input(placeholder("Text to reverse"), onInput(s => Msg.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems)
    )

  private val myStyle =
    styles(
      "width"      -> "100%",
      "height"     -> "40px",
      "padding"    -> "10px 0",
      "font-size"  -> "2em",
      "text-align" -> "center"
    )

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view)

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg:
    case Increment, Decrement

  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))(text("-")),
      div(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1

final case class Model(field: String, components: List[Counter.Model])
object Model:
  val init: Model =
    Model("", Nil)
