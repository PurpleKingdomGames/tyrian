package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.typedarray

/** Given the id of a file input field that has had a file selected, this Cmd will read either raw bytes, an image or
  * text file to return a `IArray[Byte]` or an `HTMLImageElement` or `String` respectively.
  */
object FileReader:

  val readImageCast: Result[js.Any] => Result[String] =
    case Result.Error(msg)  => Result.Error(msg)
    case Result.NoFile(msg) => Result.NoFile(msg)
    case Result.File(n, p, d) =>
      try Result.File(n, p, d.asInstanceOf[String])
      catch case _ => Result.Error("File is not a base64 string of image data")

  val readTextCast: Result[js.Any] => Result[String] =
    case Result.Error(msg)  => Result.Error(msg)
    case Result.NoFile(msg) => Result.NoFile(msg)
    case Result.File(n, p, d) =>
      try Result.File(n, p, d.asInstanceOf[String])
      catch case _ => Result.Error("File is not text")

  val readBytesCast: Result[js.Any] => Result[IArray[Byte]] =
    case Result.Error(msg)  => Result.Error(msg)
    case Result.NoFile(msg) => Result.NoFile(msg)
    case Result.File(n, p, d) =>
      try Result.File(n, p, IArray.from(new typedarray.Int8Array(d.asInstanceOf[typedarray.ArrayBuffer])))
      catch case _ => Result.Error("Could not cast loaded file data to byte array")

  /** Reads an input file from an input field as base64 encoded image data */
  def readImage[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(inputFieldId, ReadType.AsDataUrl)(readImageCast andThen resultToMessage)

  /** Reads an input file as base64 encoded image data */
  def readImage[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(file, ReadType.AsDataUrl)(readImageCast andThen resultToMessage)

  /** Reads an input file from an input field as plain text */
  def readText[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(inputFieldId, ReadType.AsText)(readTextCast andThen resultToMessage)

  /** Reads an input file as plain text */
  def readText[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(file, ReadType.AsText)(readTextCast andThen resultToMessage)

  /** Reads an input file from an input field as bytes */
  def readBytes[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[IArray[Byte]] => Msg): Cmd[F, Msg] =
    readFile(inputFieldId, ReadType.AsArrayBuffer)(readBytesCast andThen resultToMessage)

  /** Reads an input file as bytes */
  def readBytes[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[IArray[Byte]] => Msg): Cmd[F, Msg] =
    readFile(file, ReadType.AsArrayBuffer)(readBytesCast andThen resultToMessage)

  private def readFile[F[_]: Async, Msg](fileOrInputId: dom.File | String, readAsType: ReadType)(
      resultToMessage: Result[js.Any] => Msg
  ): Cmd[F, Msg] =
    val task = Async[F].delay {
      val maybeFile = fileOrInputId match
        case f: dom.File => Some(f)
        case inputId: String =>
          document.getElementById(inputId).asInstanceOf[html.Input].files.headOption
      maybeFile match
        case None => Future.successful(Result.NoFile("No files on specified input"))
        case Some(file) =>
          val p          = Promise[Result[js.Any]]()
          val fileReader = new dom.FileReader()
          fileReader.addEventListener(
            "load",
            (e: Event) =>
              p.success(
                Result.File(
                  name = file.name,
                  path = readAsType match
                    case ReadType.AsDataUrl => e.target.asInstanceOf[js.Dynamic].result.asInstanceOf[String]
                    case _                  => ""
                  ,
                  data = fileReader.result
                )
              ),
            false
          )
          fileReader.onerror = _ => p.success(Result.Error(s"Error reading from file"))

          readAsType match
            case ReadType.AsText => fileReader.readAsText(file)
            case ReadType.AsArrayBuffer =>
              fileReader.readAsArrayBuffer(file)
            case ReadType.AsDataUrl => fileReader.readAsDataURL(file)

          p.future
    }

    Cmd.Run(Async[F].fromFuture(task), resultToMessage)

  private enum ReadType derives CanEqual:
    case AsText
    case AsArrayBuffer
    case AsDataUrl

  enum Result[A]:
    case Error(message: String)
    case File(name: String, path: String, data: A)
    case NoFile(message: String)
