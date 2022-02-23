package tyrian.websocket

import org.scalajs.dom
import tyrian.Cmd
import tyrian.Sub
import tyrian.Task
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

final case class KeepAliveSettings(message: String, timeout: FiniteDuration, enabled: Boolean)
object KeepAliveSettings:
  def default  = KeepAliveSettings("{ \"Heartbeat\": {} }", 20.seconds, true)
  def disabled = default.copy(enabled = false)

object WebSocket:
  /** Acquires a WebSocket connection with default keep-alive message */
  def connect(address: String): Either[String, WebSocket] =
    newConnection(address, None, KeepAliveSettings.default).map(WebSocket(_))

  /** Acquires a WebSocket connection with default keep-alive message and a custom message onOpen */
  def connect(address: String, onOpenMessage: String): Either[String, WebSocket] =
    newConnection(address, Option(onOpenMessage), KeepAliveSettings.default).map(WebSocket(_))

  /** Acquires a WebSocket connection with custom keep-alive message */
  def connect(address: String, keepAliveSettings: KeepAliveSettings): Either[String, WebSocket] =
    newConnection(address, None, keepAliveSettings).map(WebSocket(_))

  /** Acquires a WebSocket connection with a custom keep-alive message and a custom message onOpen */
  def connect(address: String, onOpenMessage: String, keepAliveSettings: KeepAliveSettings): Either[String, WebSocket] =
    newConnection(address, Some(onOpenMessage), keepAliveSettings).map(WebSocket(_))

  private def newConnection(
      address: String,
      onOpenSendMessage: Option[String],
      settings: KeepAliveSettings
  ): Either[String, LiveSocket] =
    try {
      val socket    = new dom.WebSocket(address)
      val keepAlive = new KeepAlive(socket, settings.message, settings.timeout)

      val subs =
        Sub.Batch(
          Sub.fromEvent("message", socket) { e =>
            Some(WebSocketEvent.Receive(e.asInstanceOf[dom.MessageEvent].data.toString))
          },
          Sub.fromEvent("error", socket) { e =>
            val msg =
              try { e.asInstanceOf[dom.ErrorEvent].message }
              catch { case _: Throwable => "Unknown" }
            Some(WebSocketEvent.Error("Web socket connection error"))
          },
          Sub.fromEvent("close", socket) { e =>
            if settings.enabled then keepAlive.cancel() else ()
            val ev = e.asInstanceOf[dom.CloseEvent]
            Some(WebSocketEvent.Close(ev.code, ev.reason))
          },
          Sub.fromEvent("open", socket) { _ =>
            onOpenSendMessage.foreach(msg => socket.send(msg))
            if settings.enabled then keepAlive.run() else ()
            Some(WebSocketEvent.Open)
          }
        )

      Right(LiveSocket(socket, subs))
    } catch {
      case e: Throwable =>
        Left(s"Error trying to set up websocket: ${e.getMessage}")
    }

  final class KeepAlive(socket: dom.WebSocket, msg: String, timeout: FiniteDuration):
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    private var timerId = 0;

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    def run(): Unit =
      if socket != null && WebSocketReadyState.fromInt(socket.readyState).isOpen then
        println("[info] Sending heartbeat 💓")
        socket.send(msg)
      timerId = dom.window.setTimeout(Functions.fun0(() => run()), timeout.toMillis.toDouble)

    def cancel(): Unit =
      if (timerId <= 0) then dom.window.clearTimeout(timerId) else ()
