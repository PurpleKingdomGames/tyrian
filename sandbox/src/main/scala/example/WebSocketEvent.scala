package example

opaque type WebSocketId = String
object WebSocketId:
  inline def apply(id: String): WebSocketId          = id
  extension (wsid: WebSocketId) def toString: String = wsid

final case class WebSocketConfig(id: WebSocketId, address: String) derives CanEqual

enum WebSocketReadyState(
    val value: Int,
    val isConnecting: Boolean,
    val isOpen: Boolean,
    val isClosing: Boolean,
    val isClosed: Boolean
) derives CanEqual:
  case CONNECTING
      extends WebSocketReadyState(
        value = 0,
        isConnecting = true,
        isOpen = false,
        isClosing = false,
        isClosed = false
      )

  case OPEN
      extends WebSocketReadyState(
        value = 1,
        isConnecting = false,
        isOpen = true,
        isClosing = false,
        isClosed = false
      )

  case CLOSING
      extends WebSocketReadyState(
        value = 2,
        isConnecting = false,
        isOpen = false,
        isClosing = true,
        isClosed = false
      )

  case CLOSED
      extends WebSocketReadyState(
        value = 3,
        isConnecting = false,
        isOpen = false,
        isClosing = false,
        isClosed = true
      )

object WebSocketReadyState:
  def fromInt(i: Int): WebSocketReadyState =
    i match {
      case 0 => CONNECTING
      case 1 => OPEN
      case 2 => CLOSING
      case 3 => CLOSED
      case _ => CLOSED
    }

enum WebSocketEvent derives CanEqual:
  def giveId: WebSocketId =
    this match {
      case WebSocketEvent.ConnectOnly(config) =>
        config.id

      case WebSocketEvent.Open(_, config) =>
        config.id

      case WebSocketEvent.Send(_, config) =>
        config.id

      case WebSocketEvent.Receive(id, _) =>
        id

      case WebSocketEvent.Error(id, _) =>
        id

      case WebSocketEvent.Close(id) =>
        id
    }

  // Send
  case ConnectOnly(webSocketConfig: WebSocketConfig)           extends WebSocketEvent
  case Open(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent
  case Send(message: String, webSocketConfig: WebSocketConfig) extends WebSocketEvent

  // Receive
  case Receive(webSocketId: WebSocketId, message: String) extends WebSocketEvent
  case Error(webSocketId: WebSocketId, error: String)     extends WebSocketEvent
  case Close(webSocketId: WebSocketId)                    extends WebSocketEvent
