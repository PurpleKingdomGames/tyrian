package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import scala.concurrent.Promise
import scala.scalajs.js

/** Given the id of a file input field that has had a file selected, this Cmd will read either an image or text file to
  * return an `HTMLImageElement` or `String` respectively.
  */
object FileReader:

  /** Generic read function, you will need to cast the result to the type you're expecting. This is normal, and is
    * because we are accessing unknown resources.
    */
  def read[F[_]: Async, Msg](fileInputFieldId: String)(
      resultToMessage: Result[js.Any] => Msg
  ): Cmd[F, Msg] =
    val files = document.getElementById(fileInputFieldId).asInstanceOf[html.Input].files
    if files.length == 0 then Cmd.None
    else read(files.item(0))(resultToMessage)

  /** Reads an input file as an image */
  def readImage[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[html.Image] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[html.Image] = {
      case Result.Error(msg)    => Result.Error(msg)
      case Result.File(n, p, d) => Result.File(n, p, d.asInstanceOf[html.Image])
    }
    read(inputFieldId)(cast andThen resultToMessage)

  def readImage[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[html.Image] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[html.Image] = {
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, d.asInstanceOf[html.Image])
        catch case _ => Result.Error("File is not an image")

    }
    read(file)(cast andThen resultToMessage)

  /** Reads an input file as plain text */
  def readText[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[String] = {
      case Result.Error(msg)    => Result.Error(msg)
      case Result.File(n, p, d) => Result.File(n, p, d.asInstanceOf[String])
    }
    read(inputFieldId)(cast andThen resultToMessage)

  def readText[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[String] = {
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, d.asInstanceOf[String])
        catch case _ => Result.Error("File is not text")
    }
    read(file)(cast andThen resultToMessage)

  private def read[F[_]: Async, Msg](file: dom.File)(
      resultToMessage: Result[js.Any] => Msg
  ): Cmd[F, Msg] =
    val task = Async[F].delay {
      val p          = Promise[Result[js.Any]]()
      val fileReader = new dom.FileReader()
      fileReader.addEventListener(
        "load",
        (e: Event) =>
          p.success(
            Result.File(
              name = file.name,
              path = e.target.asInstanceOf[js.Dynamic].result.asInstanceOf[String],
              data = fileReader.result
            )
          ),
        false
      )
      fileReader.onerror = _ => p.success(Result.Error(s"Error reading from file"))
      fileReader.readAsDataURL(file)
      p.future
    }

    Cmd.Run(Async[F].fromFuture(task), resultToMessage)

  enum Result[A]:
    case Error(message: String)
    case File(name: String, path: String, data: A)
