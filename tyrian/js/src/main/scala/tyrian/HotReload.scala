package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom
import util.Functions

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scala.util.control.NonFatal

object HotReload:

  final case class Error(message: String)

  def bootstrap[F[_]: Async, Model, Msg](key: String, decode: Option[String] => Either[String, Model])(
      resultToMessage: Either[String, Model] => Msg
  ): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(decode(Option(dom.window.localStorage.getItem(key)))), resultToMessage)

  def snapshot[F[_]: Async, Model](key: String, model: Model, encode: Model => String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      dom.window.localStorage.setItem(key, encode(model))
    }
