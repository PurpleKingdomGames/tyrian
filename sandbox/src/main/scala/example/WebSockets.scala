package example

import org.scalajs.dom

import scala.collection.mutable
import tyrian.Sub
import tyrian.Cmd
import tyrian.Task
import tyrian.Task.Observable
import tyrian.Task.Observer
import tyrian.Task.Cancelable

// TODO: A reconnect back off policy
// TODO: A number of connection attempts before abort
// TODO: A way to trigger a manual reconnect
final class WebSockets(sockets: mutable.HashMap[WebSocketId, LiveSocket]):

  // send cmd
  def send[Msg](id: WebSocketId, message: String, onError: => Msg): Cmd[Msg] =
    sockets.get(id) match
      case None =>
        Cmd.Emit(onError)

      case Some(conn) =>
        Cmd.SideEffect(() => conn.socket.send(message))

  // socket subscription
  def webSocket[Msg](config: WebSocketConfig, onOpenSendMessage: Option[String])(f: WebSocketEvent => Msg): Sub[Msg] =
    reEstablishConnection(config, onOpenSendMessage) match
      case Right(wse) => wse.map(f)
      case Left(e)    => Sub.emit(f(e))

  private def reEstablishConnection(
      config: WebSocketConfig,
      onOpenSendMessage: Option[String]
  ): Either[WebSocketEvent.Error, Sub[WebSocketEvent]] =
    sockets.get(config.id) match
      case Some(conn) =>
        WebSocketReadyState.fromInt(conn.socket.readyState) match {
          case WebSocketReadyState.CLOSING | WebSocketReadyState.CLOSED =>
            newConnection(config, onOpenSendMessage).flatMap { liveSocket =>
              sockets.remove(config.id)
              sockets.put(config.id, liveSocket)
              Right(liveSocket.subs)
            }

          case _ =>
            Right(conn.subs)
        }

      case None =>
        newConnection(config, onOpenSendMessage).flatMap { liveSocket =>
          sockets.remove(config.id)
          sockets.put(config.id, liveSocket)
          Right(liveSocket.subs)
        }

  private def newConnection(
      config: WebSocketConfig,
      onOpenSendMessage: Option[String]
  ): Either[WebSocketEvent.Error, LiveSocket] =
    try {
      val socket = new dom.WebSocket(config.address)

      val subs =
        Sub.Batch(
          Sub.fromEvent("message", socket) { e =>
            Some(WebSocketEvent.Receive(config.id, e.asInstanceOf[dom.MessageEvent].data.toString))
          },
          Sub.fromEvent("error", socket)(_ => Some(WebSocketEvent.Error(config.id, "Web socket connection error"))),
          Sub.fromEvent("close", socket)(e => Some(WebSocketEvent.Close(config.id))),
          Sub.fromEvent("open", socket) { e =>
            onOpenSendMessage.foreach(msg => socket.send(msg))
            Some(WebSocketEvent.Open(config.id))
          }
        )

      Right(LiveSocket(socket, subs))
    } catch {
      case e: Throwable =>
        Left(
          WebSocketEvent.Error(config.id, s"Error trying to set up websocket '${config.id.toString}': ${e.getMessage}")
        )
    }

object WebSockets:
  def apply(): WebSockets =
    new WebSockets(mutable.HashMap())

final case class LiveSocket(socket: dom.WebSocket, subs: Sub[WebSocketEvent])

enum WebSocketEvent derives CanEqual:
  case Open(webSocketId: WebSocketId)                     extends WebSocketEvent
  case Receive(webSocketId: WebSocketId, message: String) extends WebSocketEvent
  case Error(webSocketId: WebSocketId, error: String)     extends WebSocketEvent
  case Close(webSocketId: WebSocketId)                    extends WebSocketEvent

opaque type WebSocketId = String
object WebSocketId:
  inline def apply(id: String): WebSocketId          = id
  extension (wsid: WebSocketId) def toString: String = wsid

final case class WebSocketConfig(id: WebSocketId, address: String) derives CanEqual

enum WebSocketReadyState derives CanEqual:
  case CONNECTING, OPEN, CLOSING, CLOSED

object WebSocketReadyState:
  def fromInt(i: Int): WebSocketReadyState =
    i match {
      case 0 => CONNECTING
      case 1 => OPEN
      case 2 => CLOSING
      case 3 => CLOSED
      case _ => CLOSED
    }
