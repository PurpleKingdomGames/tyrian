package tyrian.websocket

enum WebSocketEvent derives CanEqual:
  case Open
  case Receive(message: String)
  case Error(error: String)
  case Close(code: Int, reason: String)
  case Heartbeat
