package tyrian.cmds

import org.scalajs.dom.document
import org.scalajs.dom.html
import org.scalajs.dom.raw.Event
import tyrian.Cmd

/** Given a path, this cmd will load an image and return an `HTMLImageElement` for you to make use of.
  */
object ImageLoader:

  def load[Msg](path: String)(resultToMessage: Either[ImageLoadError, html.Image] => Msg): Cmd[Msg] =
    Cmd
      .Run[ImageLoadError, html.Image] { observer =>
        val image: html.Image = document.createElement("img").asInstanceOf[html.Image]
        image.src = path
        image.onload = { (_: Event) =>
          observer.onNext(image)
        }
        image.addEventListener(
          "error",
          (_: Event) => observer.onError(ImageLoadError(s"Image load error from path '$path'", path)),
          false
        )

        () => ()
      }
      .attempt(resultToMessage)

  final case class ImageLoadError(message: String, path: String)
