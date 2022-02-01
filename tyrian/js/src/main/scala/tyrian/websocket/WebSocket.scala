package tyrian.websocket

import org.scalajs.dom
import tyrian.Cmd
import tyrian.Sub
import tyrian.Task

import scala.collection.mutable

final class WebSocket(address: String, onOpenSendMessage: Option[String]):

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.null"))
  private var liveSocket: LiveSocket = null

  def publish[Msg](message: String): Cmd[Msg] =
    Cmd.SideEffect(() => liveSocket.socket.send(message))

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def subscribe[Msg](f: WebSocketEvent => Msg): Sub[Msg] =
    def newConnToSubs: Either[WebSocketEvent.Error, LiveSocket] => Sub[Msg] = {
      case Left(e) =>
        Sub.emit(f(e))

      case Right(ls) =>
        liveSocket = ls
        ls.subs.map(f)
    }

    if liveSocket != null && WebSocketReadyState.fromInt(liveSocket.socket.readyState).isOpen then
      liveSocket.subs.map(f)
    else newConnToSubs(newConnection(address, onOpenSendMessage))

  private def newConnection(
      address: String,
      onOpenSendMessage: Option[String]
  ): Either[WebSocketEvent.Error, LiveSocket] =
    try {
      val socket = new dom.WebSocket(address)

      val subs =
        Sub.Batch(
          Sub.fromEvent("message", socket) { e =>
            Some(WebSocketEvent.Receive(e.asInstanceOf[dom.MessageEvent].data.toString))
          },
          Sub.fromEvent("error", socket)(_ => Some(WebSocketEvent.Error("Web socket connection error"))),
          Sub.fromEvent("close", socket)(e => Some(WebSocketEvent.Close)),
          Sub.fromEvent("open", socket) { e =>
            onOpenSendMessage.foreach(msg => socket.send(msg))
            Some(WebSocketEvent.Open)
          }
        )

      Right(LiveSocket(socket, subs))
    } catch {
      case e: Throwable =>
        Left(
          WebSocketEvent.Error(s"Error trying to set up websocket: ${e.getMessage}")
        )
    }

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

  def apply(address: String): WebSocket =
    new WebSocket(address, None)

  def apply(address: String, onOpenMessage: String): WebSocket =
    new WebSocket(address, Option(onOpenMessage))
