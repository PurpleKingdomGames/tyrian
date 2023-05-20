package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger
import tyrian.websocket.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.WebSocketStatus(status) =>
      val (nextWS, cmds) = model.echoSocket.update(status)
      (model.copy(echoSocket = nextWS), cmds)

    case Msg.FromSocket(message) =>
      val logWS = Logger.info[IO]("Got: " + message)
      (model.copy(log = message :: model.log), logWS)

    case Msg.ToSocket(message) =>
      val cmds: Cmd[IO, Msg] =
        Cmd.Batch(
          Logger.info("Sent: " + message),
          model.echoSocket.publish(message)
        )

      (model, cmds)

    case Msg.NoOp =>
      (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div(
      div(
        model.echoSocket.connectDisconnectButton,
        p(button(onClick(Msg.ToSocket("Hello!")))("send")),
        p("Log:"),
        p(model.log.flatMap(msg => List(text(msg), br)))
      )
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    model.echoSocket.subscribe {
      case WebSocketEvent.Error(errorMesage) =>
        Msg.FromSocket(errorMesage)

      case WebSocketEvent.Receive(message) =>
        Msg.FromSocket(message)

      case WebSocketEvent.Open =>
        Msg.FromSocket("<no message - socket opened>")

      case WebSocketEvent.Close(code, reason) =>
        Msg.FromSocket(s"<socket closed> - code: $code, reason: $reason")

      case WebSocketEvent.Heartbeat =>
        Msg.ToSocket("<💓 heartbeat 💓>")
    }

enum Msg:
  case FromSocket(message: String)
  case ToSocket(message: String)
  case WebSocketStatus(status: EchoSocket.Status)
  case NoOp

final case class Model(echoSocket: EchoSocket, log: List[String])
object Model:
  val init: Model =
    Model(EchoSocket.init, Nil)

/** Encapsulates and manages our socket connection, cleanly proxies methods, and
  * knows how to draw the right connnect/disconnect button.
  */
final case class EchoSocket(socketUrl: String, socket: Option[WebSocket[IO]]):

  def connectDisconnectButton =
    if socket.isDefined then
      button(onClick(EchoSocket.Status.Disconnecting.asMsg))("Disconnect")
    else button(onClick(EchoSocket.Status.Connecting.asMsg))("Connect")

  def update(status: EchoSocket.Status): (EchoSocket, Cmd[IO, Msg]) =
    status match
      case EchoSocket.Status.ConnectionError(err) =>
        (this, Logger.error(s"Failed to open WebSocket connection: $err"))

      case EchoSocket.Status.Connected(ws) =>
        (this.copy(socket = Some(ws)), Cmd.None)

      case EchoSocket.Status.Connecting =>
        val connect =
          WebSocket.connect[IO, Msg](
            address = socketUrl,
            onOpenMessage = "Connect me!",
            keepAliveSettings = KeepAliveSettings.default
          ) {
            case WebSocketConnect.Error(err) =>
              EchoSocket.Status.ConnectionError(err).asMsg

            case WebSocketConnect.Socket(ws) =>
              EchoSocket.Status.Connected(ws).asMsg
          }

        (this, connect)

      case EchoSocket.Status.Disconnecting =>
        val log = Logger.info[IO]("Graceful shutdown of EchoSocket connection")
        val cmds =
          socket.map(ws => Cmd.Batch(log, ws.disconnect)).getOrElse(log)

        (this.copy(socket = None), cmds)

      case EchoSocket.Status.Disconnected =>
        (this, Logger.info("WebSocket not connected yet"))

  def publish(message: String): Cmd[IO, Msg] =
    socket.map(_.publish(message)).getOrElse(Cmd.None)

  def subscribe(toMessage: WebSocketEvent => Msg): Sub[IO, Msg] =
    socket.fold(Sub.emit[IO, Msg](EchoSocket.Status.Disconnected.asMsg)) {
      _.subscribe(toMessage)
    }

object EchoSocket:

  val init: EchoSocket =
    EchoSocket("ws://localhost:8080/wsecho", None)

  enum Status:
    case Connecting
    case Connected(ws: WebSocket[IO])
    case ConnectionError(msg: String)
    case Disconnecting
    case Disconnected

    def asMsg: Msg = Msg.WebSocketStatus(this)
