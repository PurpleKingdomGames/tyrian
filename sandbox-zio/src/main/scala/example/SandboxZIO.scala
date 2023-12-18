package example

import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger
import tyrian.cmds.Random
import zio.*
import zio.interop.catz.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object SandboxZIO extends TyrianApp[Msg, Model]:

  def router: Location => Msg = Routing.externalOnly(Msg.NoOp, Msg.FollowLink(_))

  def init(flags: Map[String, String]): (Model, Cmd[Task, Msg]) =
    (Model.init, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[Task, Msg]) =
    case Msg.NoOp =>
      (model, Cmd.None)

    case Msg.FollowLink(href) =>
      (model, Nav.loadUrl(href))

    case Msg.NewRandomInt(i) =>
      (model.copy(randomNumber = i), Cmd.None)

    case Msg.NewContent(content) =>
      val cmds: Cmd[Task, Msg] =
        Logger.info[Task]("New content: " + content) |+|
          Random.int[Task].map(next => Msg.NewRandomInt(next.value))

      (model.copy(field = content), cmds)

    case Msg.Insert =>
      (model.copy(components = Counter.init :: model.components), Cmd.None)

    case Msg.Remove =>
      val cs = model.components match
        case Nil    => Nil
        case _ :: t => t

      (model.copy(components = cs), Cmd.None)

    case Msg.Modify(id, m) =>
      val cs = model.components.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      (model.copy(components = cs), Cmd.None)

  def view(model: Model): Html[Msg] =
    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div(
      div(hidden(false))("Random number: " + model.randomNumber.toString),
      div(
        a(href := "/another-page")("Internal link (will be ignored)"),
        br,
        a(href := "http://tyrian.indigoengine.io/")("Tyrian website")
      ),
      div(
        input(placeholder := "Text to reverse", onInput(s => Msg.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems)
    )

  def subscriptions(model: Model): Sub[Task, Msg] =
    Sub.None

  private val myStyle =
    styles(
      CSS.width("100%"),
      CSS.height("40px"),
      CSS.padding("10px 0"),
      CSS.`font-size`("2em"),
      CSS.`text-align`("center")
    )

enum Msg:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case NewRandomInt(i: Int)
  case FollowLink(href: String)
  case NoOp

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

final case class Model(
    field: String,
    components: List[Counter.Model],
    randomNumber: Int
)
object Model:
  val init: Model =
    Model("", Nil, 0)
