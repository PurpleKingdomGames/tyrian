package tyrian.websocket

import cats.effect.IO
import org.scalajs.dom
import tyrian.Cmd
import tyrian.Sub
import tyrian.websocket.WebSocketEvent
import util.Functions

import scala.concurrent.duration.*

final class WebSocket(liveSocket: LiveSocket):
  def disconnect[Msg]: Cmd[Msg] =
    Cmd.SideEffect(() => liveSocket.socket.close(1000, "Graceful shutdown"))

  def publish[Msg](message: String): Cmd[Msg] =
    Cmd.SideEffect(() => liveSocket.socket.send(message))

  def subscribe[Msg](f: WebSocketEvent => Msg): Sub[Msg] =
    if WebSocketReadyState.fromInt(liveSocket.socket.readyState).isOpen then liveSocket.subs.map(f)
    else Sub.emit(f(WebSocketEvent.Error("Connection not ready")))

final class LiveSocket(val socket: dom.WebSocket, val subs: Sub[WebSocketEvent])

enum WebSocketReadyState derives CanEqual:
  case CONNECTING, OPEN, CLOSING, CLOSED

  def isOpen: Boolean =
    this match
      case CLOSED  => false
      case CLOSING => false
      case _       => true

object WebSocketReadyState:
  def fromInt(i: Int): WebSocketReadyState =
    i match {
      case 0 => CONNECTING
      case 1 => OPEN
      case 2 => CLOSING
      case 3 => CLOSED
      case _ => CLOSED
    }

final case class KeepAliveSettings(timeout: FiniteDuration)
object KeepAliveSettings:
  def default = KeepAliveSettings(20.seconds)

object WebSocket:
  /** Acquires a WebSocket connection with default keep-alive message */
  def connect[Msg](address: String)(resultToMessage: WebSocketConnect => Msg): Cmd[Msg] =
    Cmd.Run(connectTask(address), resultToMessage)

  /** Acquires a WebSocket connection with default keep-alive message and a custom message onOpen */
  def connect[Msg](address: String, onOpenMessage: String)(
      resultToMessage: WebSocketConnect => Msg
  ): Cmd[Msg] =
    Cmd.Run(connectTask(address, onOpenMessage), resultToMessage)

  /** Acquires a WebSocket connection with custom keep-alive message */
  def connect[Msg](address: String, keepAliveSettings: KeepAliveSettings)(
      resultToMessage: WebSocketConnect => Msg
  ): Cmd[Msg] =
    Cmd.Run(connectTask(address, keepAliveSettings), resultToMessage)

  /** Acquires a WebSocket connection with a custom keep-alive message and a custom message onOpen */
  def connect[Msg](address: String, onOpenMessage: String, keepAliveSettings: KeepAliveSettings)(
      resultToMessage: WebSocketConnect => Msg
  ): Cmd[Msg] =
    Cmd.Run(connectTask(address, onOpenMessage, keepAliveSettings), resultToMessage)

  private def connectTask(address: String): IO[WebSocketConnect] =
    newConnection(address, None, KeepAliveSettings.default)
      .map(ls => WebSocketConnect.Socket(WebSocket(ls)))

  private def connectTask(address: String, onOpenMessage: String): IO[WebSocketConnect] =
    newConnection(address, Option(onOpenMessage), KeepAliveSettings.default)
      .map(ls => WebSocketConnect.Socket(WebSocket(ls)))

  private def connectTask(address: String, keepAliveSettings: KeepAliveSettings): IO[WebSocketConnect] =
    newConnection(address, None, keepAliveSettings)
      .map(ls => WebSocketConnect.Socket(WebSocket(ls)))

  private def connectTask(
      address: String,
      onOpenMessage: String,
      keepAliveSettings: KeepAliveSettings
  ): IO[WebSocketConnect] =
    newConnection(address, Some(onOpenMessage), keepAliveSettings)
      .map(ls => WebSocketConnect.Socket(WebSocket(ls)))

  private def newConnection(
      address: String,
      onOpenSendMessage: Option[String],
      settings: KeepAliveSettings
  ): IO[LiveSocket] =
    IO.delay {
      val socket    = new dom.WebSocket(address)
      val keepAlive = new KeepAlive(socket, settings)

      val subs =
        Sub.Batch(
          Sub.fromEvent("message", socket) { e =>
            Some(WebSocketEvent.Receive(e.asInstanceOf[dom.MessageEvent].data.toString))
          },
          Sub.fromEvent("error", socket) { e =>
            val msg =
              try e.asInstanceOf[dom.ErrorEvent].message
              catch { case _: Throwable => "Unknown" }
            Some(WebSocketEvent.Error(msg))
          },
          Sub.fromEvent("close", socket) { e =>
            val ev = e.asInstanceOf[dom.CloseEvent]
            Some(WebSocketEvent.Close(ev.code, ev.reason))
          },
          Sub.fromEvent("open", socket) { _ =>
            onOpenSendMessage.foreach(msg => socket.send(msg))
            Some(WebSocketEvent.Open)
          },
          keepAlive.run
        )

      LiveSocket(socket, subs)
    }

  final class KeepAlive(socket: dom.WebSocket, settings: KeepAliveSettings):
    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    def run: Sub[WebSocketEvent] =
      if socket != null && WebSocketReadyState.fromInt(socket.readyState).isOpen then
        Sub.every(settings.timeout, "ws-heartbeat").map(_ => WebSocketEvent.Heartbeat)
      else Sub.Empty

enum WebSocketConnect:
  case Error(msg: String)
  case Socket(webSocket: WebSocket)
