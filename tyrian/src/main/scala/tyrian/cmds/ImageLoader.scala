package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import scala.concurrent.Promise

/** Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
  */
object ImageLoader:

  /** Load an image from the given path and produce a message */
  def load[F[_]: Async, Msg](path: String)(resultToMessage: Result => Msg): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        val p                 = Promise[Result]()
        val image: html.Image = document.createElement("img").asInstanceOf[html.Image]
        image.src = path
        image.onload = { (_: Event) =>
          p.success(Result.Image(image))
        }
        image.addEventListener(
          "error",
          (_: Event) => p.success(Result.ImageLoadError(s"Image load error from path '$path'", path)),
          false
        )
        p.future
      }
    Cmd.Run(Async[F].fromFuture(task), resultToMessage)

  enum Result:
    case Image(img: html.Image)
    case ImageLoadError(message: String, path: String)
