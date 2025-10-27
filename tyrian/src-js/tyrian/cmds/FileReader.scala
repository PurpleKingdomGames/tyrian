package tyrian.cmds

import cats.effect.kernel.Async
import cats.syntax.all.*
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import scala.scalajs.js
import scala.scalajs.js.typedarray

/** Given the id of a file input field that has had a file selected, this Cmd will read either raw bytes, an image or
  * text file to return a `IArray[Byte]` or an `HTMLImageElement` or `String` respectively.
  */
object FileReader:

  private val readImageCast: Result[js.Any] => Result[String] =
    case Result.Error(msg)  => Result.Error(msg)
    case Result.NoFile(msg) => Result.NoFile(msg)
    case Result.File(n, p, d) =>
      try Result.File(n, p, d.asInstanceOf[String])
      catch case _ => Result.Error("File is not a base64 string of image data")

  private val readTextCast: Result[js.Any] => Result[String] =
    case Result.Error(msg)  => Result.Error(msg)
    case Result.NoFile(msg) => Result.NoFile(msg)
    case Result.File(n, p, d) =>
      try Result.File(n, p, d.asInstanceOf[String])
      catch case _ => Result.Error("File is not text")

  private val readBytesCast: Result[js.Any] => Result[IArray[Byte]] =
    case Result.Error(msg)  => Result.Error(msg)
    case Result.NoFile(msg) => Result.NoFile(msg)
    case Result.File(n, p, d) =>
      try Result.File(n, p, IArray.from(new typedarray.Int8Array(d.asInstanceOf[typedarray.ArrayBuffer])))
      catch case _ => Result.Error("Could not cast loaded file data to byte array")

  /** Reads an input file from an input field as base64 encoded image data */
  def readImage[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(getFileFromInput(inputFieldId), ReadType.AsDataUrl)(readImageCast andThen resultToMessage)

  /** Reads an input file as base64 encoded image data */
  def readImage[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(Some(file).pure[F], ReadType.AsDataUrl)(readImageCast andThen resultToMessage)

  /** Reads an input file from an input field as plain text */
  def readText[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(getFileFromInput(inputFieldId), ReadType.AsText)(readTextCast andThen resultToMessage)

  /** Reads an input file as plain text */
  def readText[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    readFile(Some(file).pure[F], ReadType.AsText)(readTextCast andThen resultToMessage)

  /** Reads an input file from an input field as bytes */
  def readBytes[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[IArray[Byte]] => Msg): Cmd[F, Msg] =
    readFile(getFileFromInput(inputFieldId), ReadType.AsArrayBuffer)(readBytesCast andThen resultToMessage)

  /** Reads an input file as bytes */
  def readBytes[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[IArray[Byte]] => Msg): Cmd[F, Msg] =
    readFile(Some(file).pure[F], ReadType.AsArrayBuffer)(readBytesCast andThen resultToMessage)

  private def getFileFromInput[F[_]: Async](inputFieldId: String): F[Option[dom.File]] =
    Async[F].delay(document.getElementById(inputFieldId).asInstanceOf[html.Input].files.headOption)

  private def readFile[F[_]: Async, Msg](maybeGetFile: F[Option[dom.File]], readAsType: ReadType)(
      resultToMessage: Result[js.Any] => Msg
  ): Cmd[F, Msg] =
    val task: F[Result[js.Any]] = maybeGetFile.flatMap {
      case None => Result.NoFile("No files on specified input").pure[F]
      case Some(file) =>
        Async[F].async { callback =>
          Async[F].delay {
            val fileReader = new dom.FileReader()
            fileReader.addEventListener(
              "load",
              (e: Event) =>
                callback(
                  Right(
                    Result.File(
                      name = file.name,
                      path = readAsType match
                        case ReadType.AsDataUrl => e.target.asInstanceOf[js.Dynamic].result.asInstanceOf[String]
                        case _                  => ""
                      ,
                      data = fileReader.result
                    )
                  )
                ),
              false
            )
            fileReader.onerror = _ => callback(Right(Result.Error(s"Error reading from file")))

            readAsType match
              case ReadType.AsText => fileReader.readAsText(file)
              case ReadType.AsArrayBuffer =>
                fileReader.readAsArrayBuffer(file)
              case ReadType.AsDataUrl => fileReader.readAsDataURL(file)

            None
          }
        }
    }

    Cmd.Run(task, resultToMessage)

  private enum ReadType derives CanEqual:
    case AsText
    case AsArrayBuffer
    case AsDataUrl

  enum Result[A]:
    case Error(message: String)
    case File(name: String, path: String, data: A)
    case NoFile(message: String)
