package tyrian.websocket

enum WebSocketEvent derives CanEqual:
  case Open                     extends WebSocketEvent
  case Receive(message: String) extends WebSocketEvent
  case Error(error: String)     extends WebSocketEvent
  case Close                    extends WebSocketEvent
