package example

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
