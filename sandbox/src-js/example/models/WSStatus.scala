package example.models

import cats.effect.IO
import tyrian.websocket.WebSocket
import example.Msg

enum WSStatus:
  case Connecting
  case Connected(ws: WebSocket[IO])
  case ConnectionError(msg: String)
  case Disconnecting
  case Disconnected

  def asMsg: Msg = Msg.WebSocketStatus(this)
