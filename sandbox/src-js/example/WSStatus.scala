package example

import cats.effect.IO
import tyrian.websocket.WebSocket

enum WSStatus:
  case Connecting
  case Connected(ws: WebSocket[IO])
  case ConnectionError(msg: String)
  case Disconnecting
  case Disconnected

  def asMsg: Msg = Msg.WebSocketStatus(this)
