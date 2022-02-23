package tyrian.websocket

import org.scalajs.dom
import tyrian.Cmd
import tyrian.Sub
import tyrian.Task
import tyrian.websocket.WebSocketEvent
import util.Functions

final class WebSocket(liveSocket: LiveSocket):
  def publish[Msg](message: String): Cmd[Msg] =
    Cmd.SideEffect(() => liveSocket.socket.send(message))

  def subscribe[Msg](f: WebSocketEvent => Msg): Sub[Msg] =
    if WebSocketReadyState.fromInt(liveSocket.socket.readyState).isOpen then liveSocket.subs.map(f)
    else Sub.emit(f(WebSocketEvent.Close))

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

object WebSocket:
  /** Acquires a WebSocket connection with default keep-alive message */
  def connect(address: String): Either[String, WebSocket] =
    newConnection(address, None, None, true).map(WebSocket(_))

  /** Acquires a WebSocket connection with default keep-alive message and a custom message onOpen */
  def connect(address: String, onOpenMessage: String): Either[String, WebSocket] =
    newConnection(address, Option(onOpenMessage), None, true).map(WebSocket(_))

  /** Acquires a WebSocket connection with custom keep-alive message */
  def connect(address: String, keepAliveMessage: Option[String]): Either[String, WebSocket] =
    newConnection(address, None, keepAliveMessage, true).map(WebSocket(_))

  /** Acquires a WebSocket connection with a custom keep-alive message and a custom message onOpen */
  def connect(address: String, onOpenMessage: String, keepAliveMessage: Option[String]): Either[String, WebSocket] =
    newConnection(address, Some(onOpenMessage), keepAliveMessage, true).map(WebSocket(_))

  /** Acquires a WebSocket connection with disabled keep-alive mechanism */
  def noKeepAlive(address: String): Either[String, WebSocket] =
    newConnection(address, None, None, false).map(WebSocket(_))

  private def newConnection(
      address: String,
      onOpenSendMessage: Option[String],
      withKeepAliveMessage: Option[String],
      keepAliveEnabled: Boolean
  ): Either[String, LiveSocket] =
    try {
      val socket    = new dom.WebSocket(address)
      val keepAlive = new KeepAlive(socket, withKeepAliveMessage, 20000)

      val subs =
        Sub.Batch(
          Sub.fromEvent("message", socket) { e =>
            Some(WebSocketEvent.Receive(e.asInstanceOf[dom.MessageEvent].data.toString))
          },
          Sub.fromEvent("error", socket) { _ =>
            Some(WebSocketEvent.Error("Web socket connection error"))
          },
          Sub.fromEvent("close", socket) { _ =>
            if keepAliveEnabled then keepAlive.cancel() else ()
            Some(WebSocketEvent.Close)
          },
          Sub.fromEvent("open", socket) { _ =>
            onOpenSendMessage.foreach(msg => socket.send(msg))
            if keepAliveEnabled then keepAlive.run() else ()
            Some(WebSocketEvent.Open)
          }
        )

      Right(LiveSocket(socket, subs))
    } catch {
      case e: Throwable =>
        Left(s"Error trying to set up websocket: ${e.getMessage}")
    }

  final class KeepAlive(socket: dom.WebSocket, msg: Option[String], timeout: Int):
    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    private var timerId = 0;

    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    def run(): Unit =
      if socket != null && WebSocketReadyState.fromInt(socket.readyState).isOpen then
        println("[info] Sending heartbeat 💓")
        socket.send(msg.getOrElse("{ \"Heartbeat\": {} }"))
      timerId = dom.window.setTimeout(Functions.fun0(() => run()), timeout)

    def cancel(): Unit =
      if (timerId <= 0) then dom.window.clearTimeout(timerId) else ()
