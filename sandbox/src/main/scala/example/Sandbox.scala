package example

import tyrian._
import tyrian.Html._
import org.scalajs.dom.document
import org.scalajs.dom.Element

object Sandbox extends TyrianApp[Msg, Model] with WebSockets:

  def container: Element = document.getElementById("myapp")

  def init: (Model, Cmd[Msg]) =
    (Model.init, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
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
        (model, send(WebSocketId("my connection"), message))

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
        input(placeholder := "Text to reverse", onInput(s => Msg.NewContent(s)), myStyle),
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

  val process: Either[WebSocketEvent.Error, WebSocketEvent] => Msg = {
    case Left(WebSocketEvent.Error(id, errorMesage)) =>
      println("Got, Error: " + errorMesage)
      Msg.FromSocket(errorMesage)

    case Right(WebSocketEvent.Receive(id, message)) =>
      println("Got, Receive: " + message)
      Msg.FromSocket(message)

    case e =>
      println("Got, Other: " + e.toString)
      Msg.FromSocket("unknown event: " + e.toString)
  }

  def subscriptions(model: Model): Sub[Msg] =
    webSocket(
      WebSocketConfig(
        WebSocketId("my connection"),
        "ws://localhost:8080/wsecho"
      ),
      Option("Connect me!"),
      process
    )

enum Msg:
  case NewContent(content: String)      extends Msg
  case Insert                           extends Msg
  case Remove                           extends Msg
  case Modify(i: Int, msg: Counter.Msg) extends Msg
  case FromSocket(message: String)      extends Msg
  case ToSocket(message: String)        extends Msg

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

final case class Model(field: String, components: List[Counter.Model], log: List[String])
object Model:
  val init: Model =
    Model("", Nil, Nil)
