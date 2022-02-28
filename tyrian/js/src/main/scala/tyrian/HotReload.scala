package tyrian

import org.scalajs.dom
import util.Functions

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js
import scala.util.control.NonFatal

object HotReload:

  final case class Error(message: String)

  def bootstrap[Model, Msg](key: String, decode: Option[String] => Model)(
      resultToMessage: Either[Error, Model] => Msg
  ): Cmd[Msg] =
    Cmd.Run(resultToMessage) { observer =>
      try
        observer.onNext(decode(Option(dom.window.localStorage.getItem(key))))
      catch {
        case NonFatal(e) =>
          observer.onError(Error(e.getMessage))
      }

      () => ()
    }

  def snapshot[Model](key: String, model: Model, encode: Model => String): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      dom.window.localStorage.setItem(key, encode(model))
    }
