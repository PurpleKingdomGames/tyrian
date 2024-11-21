package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

/** Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
  */
object ImageLoader:

  /** Load an image from the given path and produce a message */
  def load[F[_]: Async, Msg](path: String)(resultToMessage: Result => Msg): Cmd[F, Msg] =
    val task: F[Result] =
      Async[F].async { callback =>
        Async[F].delay {
          val image: html.Image = document.createElement("img").asInstanceOf[html.Image]
          image.src = path
          image.onload = { (_: Event) =>
            callback(Right(Result.Image(image)))
          }
          image.addEventListener(
            "error",
            (_: Event) => callback(Right(Result.ImageLoadError(s"Image load error from path '$path'", path))),
            false
          )
          None
        }
      }

    Cmd.Run(task, resultToMessage)

  enum Result:
    case Image(img: html.Image)
    case ImageLoadError(message: String, path: String)
