package example

import tyrian.Html.*
import tyrian.*
import tyrian.websocket.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
    (Model.init, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.FromSocket(message) =>
        println("Got: " + message)
        (model.copy(log = message :: model.log), Cmd.Empty)

      case Msg.ToSocket(message) =>
        println("Sent: " + message)
        (model, model.echoSocket.publish(message))

  def view(model: Model): Html[Msg] =
    div(
      div(
        p(button(onClick(Msg.ToSocket("Hello!")))(text("send"))),
        p("Log:"),
        p(model.log.flatMap(msg => List(text(msg), br)))
      )
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
  case FromSocket(message: String) extends Msg
  case ToSocket(message: String)   extends Msg

final case class Model(echoSocket: WebSocket, log: List[String])
object Model:
  val init: Model =
    Model(WebSocket("ws://localhost:8080/wsecho", "Connect me!"), Nil)
