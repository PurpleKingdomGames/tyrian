package tyrian.websocket

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.effect.std.Queue
import cats.syntax.functor.*
import fs2.Stream
import org.scalajs.dom
import tyrian.Cmd
import tyrian.Sub
import tyrian.websocket.WebSocketEvent
import util.Functions

import scala.concurrent.duration.*

/** Helper WebSocket instance to store in your model that acts as a controller */
final class WebSocket[F[_]: Async](liveSocket: LiveSocket[F]):
  /** Disconnect from this WebSocket */
  def disconnect[Msg]: Cmd[F, Msg] =
    Cmd.SideEffect(liveSocket.socket.close(1000, "Graceful shutdown"))

  /** Publish a message to this WebSocket */
  def publish[Msg](message: String): Cmd[F, Msg] =
    Cmd.SideEffect(liveSocket.socket.send(message))

  /** Subscribe to messages from this WebSocket */
  def subscribe[Msg](f: WebSocketEvent => Msg): Sub[F, Msg] =
    if WebSocketReadyState.fromInt(liveSocket.socket.readyState).isOpen then liveSocket.subs.map(f)
    else Sub.emit(f(WebSocketEvent.Error("Connection not ready")))

/** The running instance of the WebSocket */
final class LiveSocket[F[_]: Async](val socket: dom.WebSocket, val subs: Sub[F, WebSocketEvent])

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

final case class KeepAliveSettings(timeout: FiniteDuration)
object KeepAliveSettings:
  def default = KeepAliveSettings(20.seconds)

object WebSocket:
  /** Acquires a WebSocket connection with default keep-alive message */
  def connect[F[_]: Async, Msg](address: String)(resultToMessage: WebSocketConnect[F] => Msg): Cmd[F, Msg] =
    Cmd.BuildRun(connectTask(address, _), resultToMessage)

  /** Acquires a WebSocket connection with default keep-alive message and a custom message onOpen */
  def connect[F[_]: Async, Msg](address: String, onOpenMessage: String)(
      resultToMessage: WebSocketConnect[F] => Msg
  ): Cmd[F, Msg] =
    Cmd.BuildRun(connectTask(address, onOpenMessage, _), resultToMessage)

  /** Acquires a WebSocket connection with custom keep-alive message */
  def connect[F[_]: Async, Msg](address: String, keepAliveSettings: KeepAliveSettings)(
      resultToMessage: WebSocketConnect[F] => Msg
  ): Cmd[F, Msg] =
    Cmd.BuildRun(connectTask(address, keepAliveSettings, _), resultToMessage)

  /** Acquires a WebSocket connection with a custom keep-alive message and a custom message onOpen */
  def connect[F[_]: Async, Msg](address: String, onOpenMessage: String, keepAliveSettings: KeepAliveSettings)(
      resultToMessage: WebSocketConnect[F] => Msg
  ): Cmd[F, Msg] =
    Cmd.BuildRun(connectTask(address, onOpenMessage, keepAliveSettings, _), resultToMessage)

  private def connectTask[F[_]: Async](address: String, disp: Dispatcher[F]): F[WebSocketConnect[F]] =
    Async[F].map(newConnection(address, None, KeepAliveSettings.default, disp))(ls => WebSocketConnect.Socket(WebSocket(ls)))

  private def connectTask[F[_]: Async](address: String, onOpenMessage: String, disp: Dispatcher[F]): F[WebSocketConnect[F]] =
    Async[F].map(newConnection(address, Option(onOpenMessage), KeepAliveSettings.default, disp))(ls =>
      WebSocketConnect.Socket(WebSocket(ls))
    )

  private def connectTask[F[_]: Async](address: String, keepAliveSettings: KeepAliveSettings, disp: Dispatcher[F]): F[WebSocketConnect[F]] =
    Async[F].map(newConnection(address, None, keepAliveSettings, disp))(ls => WebSocketConnect.Socket(WebSocket(ls)))

  private def connectTask[F[_]: Async](
      address: String,
      onOpenMessage: String,
      keepAliveSettings: KeepAliveSettings,
      disp: Dispatcher[F]
  ): F[WebSocketConnect[F]] =
    Async[F].map(newConnection(address, Some(onOpenMessage), keepAliveSettings, disp))(ls =>
      WebSocketConnect.Socket(WebSocket(ls))
    )

  private def newConnection[F[_]: Async](
      address: String,
      onOpenSendMessage: Option[String],
      settings: KeepAliveSettings,
      disp: Dispatcher[F]
  ): F[LiveSocket[F]] =
    // XXX - How to get flatMap into scope for a for-comprehension?
    Async[F].flatMap(Queue.unbounded[F, WebSocketEvent]) { q =>
      Async[F].delay {
        val socket    = new dom.WebSocket(address)
        val keepAlive = new KeepAlive(socket, settings)

        val msgListener = Functions.fun { e =>
          val event = WebSocketEvent.Receive(e.asInstanceOf[dom.MessageEvent].data.toString)
          disp.unsafeRunAndForget(q.offer(event))
        }

        val errListener = Functions.fun { e =>
          val msg =
            try e.asInstanceOf[dom.ErrorEvent].message
            catch { case _: Throwable => "Unknown" }
          disp.unsafeRunAndForget(q.offer(WebSocketEvent.Error(msg)))
        }

        val closeListener = Functions.fun { e =>
          val ev = e.asInstanceOf[dom.CloseEvent]
          disp.unsafeRunAndForget(q.offer(WebSocketEvent.Close(ev.code, ev.reason)))
        }

        val openListener = Functions.fun { _ =>
          onOpenSendMessage.foreach(msg => socket.send(msg))
          disp.unsafeRunAndForget(q.offer(WebSocketEvent.Open))
        }

        socket.addEventListener("message", msgListener)
        socket.addEventListener("error", errListener)
        socket.addEventListener("open", openListener)
        socket.addEventListener("close", closeListener)

        val subs =
          Sub.Batch(
            Sub.make(s"[tyrian-ws-${address}]")(
              Stream.repeatEval {
                q.take
              }
            )(
              Async[F].delay {
                socket.removeEventListener("message", msgListener)
                socket.removeEventListener("error", errListener)
                socket.removeEventListener("open", openListener)
                socket.removeEventListener("close", closeListener)
              }
            ),
            keepAlive.run
          )

        LiveSocket(socket, subs)
      }
    }

  final class KeepAlive[F[_]: Async](socket: dom.WebSocket, settings: KeepAliveSettings):
    @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
    def run: Sub[F, WebSocketEvent] =
      if socket != null && WebSocketReadyState.fromInt(socket.readyState).isOpen then
        Sub.every(settings.timeout, "[tyrian-ws-heartbeat]").as(WebSocketEvent.Heartbeat)
      else Sub.None

sealed trait WebSocketConnect[F[_]: Async]
object WebSocketConnect:
  final case class Error[F[_]: Async](msg: String)              extends WebSocketConnect[F]
  final case class Socket[F[_]: Async](webSocket: WebSocket[F]) extends WebSocketConnect[F]
