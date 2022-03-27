package tyrian

import cats.effect.IO
import org.scalajs.dom
import util.Functions

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scala.util.control.NonFatal

object HotReload:

  final case class Error(message: String)

  def bootstrap[Model, Msg](key: String, decode: Option[String] => Either[String, Model])(
      resultToMessage: Either[String, Model] => Msg
  ): Cmd[Msg] =
    Cmd.Run(IO(decode(Option(dom.window.localStorage.getItem(key)))), resultToMessage)

  def snapshot[Model](key: String, model: Model, encode: Model => String): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      dom.window.localStorage.setItem(key, encode(model))
    }
