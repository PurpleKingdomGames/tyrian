package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom

/** A very simple mechanism to allow automatic loading and saving of your applications model to local storage. Uses:
  *
  *   - During development, allows you to carry on where you left off between site rebuilds.
  *   - Acts as a user session, to remember where a website/app visitor/user was when they left.
  */
object HotReload:

  final case class Error(message: String)

  /** Used during `init` to try and load an existing session */
  def bootstrap[F[_]: Async, Model, Msg](key: String, decode: Option[String] => Either[String, Model])(
      resultToMessage: Either[String, Model] => Msg
  ): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(decode(Option(dom.window.localStorage.getItem(key)))), resultToMessage)

  /** Simple command to store the model. Can be performed on a regular basis if used in combination with a `Sub.every`
    * subscription.
    */
  def snapshot[F[_]: Async, Model](key: String, model: Model, encode: Model => String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      dom.window.localStorage.setItem(key, encode(model))
    }
