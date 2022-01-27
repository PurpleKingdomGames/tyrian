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

  private val connections: mutable.HashMap[WebSocketId, dom.WebSocket] = mutable.HashMap()
  private val configs: mutable.HashMap[WebSocketId, WebSocketConfig]   = mutable.HashMap()

  // send cmd
  def send(id: WebSocketId, message: String): Cmd[Nothing] =
    connections.get(id) match
      case None =>
        // TODO: Return an error?
        Cmd.Empty

      case Some(conn) =>
        Cmd.SideEffect(() => conn.send(message))

  // connection sub
  // TODO: Error separate - not an event
  // TODO: Events are actually... states?
  // TODO: Subs only produce connection, error, and received type messages
  def webSocket[Msg](
      config: WebSocketConfig,
      onOpenSendMessage: Option[String],
      f: Either[WebSocketEvent.Error, WebSocketEvent] => Msg
  ): Sub[Msg] =
    insertUpdateConfig(config)
    Sub.OfObservable(
      config.id.toString,
      socketObservable(config, onOpenSendMessage),
      f
    )

  private def socketObservable[Msg](
      config: WebSocketConfig,
      onOpenSendMessage: Option[String]
  ): Observable[WebSocketEvent.Error, Msg] =
    Task
      .RunObservable[WebSocketEvent.Error, Msg] { observer =>
        reEstablishConnection(config, onOpenSendMessage) match
          case Left(e) =>
            observer.onError(WebSocketEvent.Error(config.id, e))
            () => ()

          case Right(socket) =>
            () => socket.close(-1, "WebSocket closed automatically")
      }
      .observable

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
  ): Either[String, dom.WebSocket] =
    connections.get(config.id) match
      case Some(conn) =>
        WebSocketReadyState.fromInt(conn.readyState) match {
          case WebSocketReadyState.CLOSING | WebSocketReadyState.CLOSED =>
            newConnection(config, onOpenSendMessage).flatMap { newConn =>
              connections.remove(config.id)
              connections.put(config.id, newConn)
              Right(newConn)
            }

          case _ =>
            Right(conn)
        }

      case None =>
        newConnection(config, onOpenSendMessage).flatMap { newConn =>
          connections.remove(config.id)
          connections.put(config.id, newConn)
          Right(newConn)
        }

  private def newConnection(config: WebSocketConfig, onOpenSendMessage: Option[String]): Either[String, dom.WebSocket] =
    try {
      val socket = new dom.WebSocket(config.address)

      // TODO: I'm missing this sort of this: observer.onNext(..)
      // TODO: Events are being produced but going nowhere.

      socket.onmessage = (e: dom.MessageEvent) =>
        println("ws recieve: " + e.data.toString)
        WebSocketEvent.Receive(config.id, e.data.toString)

      socket.onopen = (_: dom.Event) => onOpenSendMessage.foreach(msg => socket.send(msg))
      socket.onerror = (_: dom.Event) => WebSocketEvent.Error(config.id, "Web socket connection error")
      socket.onclose = (_: dom.CloseEvent) => WebSocketEvent.Close(config.id)

      Right(socket)
    } catch {
      case e: Throwable =>
        Left("Error trying to set up a websocket: " + e.getMessage)
    }
