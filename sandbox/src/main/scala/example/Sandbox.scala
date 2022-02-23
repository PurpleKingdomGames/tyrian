package example

import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Dom
import tyrian.cmds.Logger
import tyrian.websocket.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Sandbox extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
    (Model.init, Logger.info(flags.toString))

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Log(msg) =>
        (model, Logger.info(msg))

      case Msg.FocusOnInputField =>
        val cmd = Dom.focus("text-reverse-field") {
          case Left(Dom.NotFound(id)) => Msg.Log("Element not found: " + id)
          case _                      => Msg.Log("Focused on input field")
        }
        (model, cmd)

      case Msg.NewContent(content) =>
        (model.copy(field = content), Cmd.Empty)

      case Msg.Insert =>
        (model.copy(components = Counter.init :: model.components), Cmd.Empty)

      case Msg.Remove =>
        val cs = model.components match
          case Nil    => Nil
          case _ :: t => t

        (model.copy(components = cs), Cmd.Empty)

      case Msg.Modify(id, m) =>
        val cs = model.components.zipWithIndex.map { case (c, i) =>
          if i == id then Counter.update(m, c) else c
        }

        (model.copy(components = cs), Cmd.Empty)

      case Msg.FromSocket(message) =>
        println("Got: " + message)
        (model.copy(log = message :: model.log), Cmd.Empty)

      case Msg.ToSocket(message) =>
        println("Sent: " + message)
        (model, model.echoSocket.publish(message))

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
        button(onClick(Msg.FocusOnInputField))("Focus on the textfield"),
        input(id := "text-reverse-field", placeholder := "Text to reverse", onInput(s => Msg.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems),
      div(
        p(button(onClick(Msg.ToSocket("Hello!")))(text("send"))),
        p("Log:"),
        p(model.log.flatMap(msg => List(text(msg), br)))
      )
    )

  private val myStyle =
    styles(
      "width"      -> "100%",
      "height"     -> "40px",
      "padding"    -> "10px 0",
      "font-size"  -> "2em",
      "text-align" -> "center"
    )

  def subscriptions(model: Model): Sub[Msg] =
    model.echoSocket.subscribe {
      case WebSocketEvent.Error(errorMesage) =>
        Msg.FromSocket(errorMesage)

      case WebSocketEvent.Receive(message) =>
        Msg.FromSocket(message)

      case WebSocketEvent.Open =>
        Msg.FromSocket("<no message - socket opened>")

      case WebSocketEvent.Close =>
        Msg.FromSocket("<no message - socket closed>")
    }

enum Msg:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case FromSocket(message: String)
  case ToSocket(message: String)
  case FocusOnInputField
  case Log(msg: String)

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

final case class Model(echoSocket: WebSocket, field: String, components: List[Counter.Model], log: List[String])
object Model:
  val init: Model =
    Model(WebSocket("ws://localhost:8080/wsecho", "Connect me!"), "", Nil, Nil)
