package example

import org.scalajs.dom

import scala.collection.mutable
import tyrian.Sub
import tyrian.Cmd
import tyrian.Task
import tyrian.Task.Observable
import tyrian.Task.Observer
import tyrian.Task.Cancelable

trait WebSockets:

  private val connections: mutable.HashMap[WebSocketId, LiveSocket]  = mutable.HashMap()
  private val configs: mutable.HashMap[WebSocketId, WebSocketConfig] = mutable.HashMap()

  // send cmd
  def send[Msg](id: WebSocketId, message: String, onError: => Msg): Cmd[Msg] =
    connections.get(id) match
      case None =>
        Cmd.Emit(onError)

      case Some(conn) =>
        Cmd.SideEffect(() => conn.socket.send(message))

  // socket subscription
  def webSocket[Msg](config: WebSocketConfig, onOpenSendMessage: Option[String])(f: WebSocketEvent => Msg): Sub[Msg] =
    insertUpdateConfig(config)
    reEstablishConnection(config, onOpenSendMessage) match
      case Right(wse) => wse.map(f)
      case Left(e)    => Sub.emit(f(e))

  private def insertUpdateConfig(config: WebSocketConfig): WebSocketConfig = {
    val maybeConfig = configs.get(config.id)

    maybeConfig
      .flatMap { c =>
        if (c == config) Option(c)
        else {
          configs.remove(config.id)
          configs.put(config.id, config)
        }
      }
      .getOrElse(config)
  }

  private def reEstablishConnection(
      config: WebSocketConfig,
      onOpenSendMessage: Option[String]
  ): Either[WebSocketEvent.Error, Sub[WebSocketEvent]] =
    connections.get(config.id) match
      case Some(conn) =>
        WebSocketReadyState.fromInt(conn.socket.readyState) match {
          case WebSocketReadyState.CLOSING | WebSocketReadyState.CLOSED =>
            newConnection(config, onOpenSendMessage).flatMap { liveSocket =>
              connections.remove(config.id)
              connections.put(config.id, liveSocket)
              Right(liveSocket.subs)
            }

          case _ =>
            Right(conn.subs)
        }

      case None =>
        newConnection(config, onOpenSendMessage).flatMap { liveSocket =>
          connections.remove(config.id)
          connections.put(config.id, liveSocket)
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

  final case class LiveSocket(socket: dom.WebSocket, subs: Sub[WebSocketEvent])
