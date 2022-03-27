package tyrian.cmds

import cats.effect.IO
import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import tyrian.Cmd

import scala.concurrent.Promise

/** Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
  */
object ImageLoader:

  def load[Msg](path: String)(resultToMessage: Result => Msg): Cmd[Msg] =
    val task =
      IO {
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
    Cmd.Run(IO.fromFuture(task), resultToMessage)

  enum Result:
    case Image(img: html.Image)
    case ImageLoadError(message: String, path: String)
