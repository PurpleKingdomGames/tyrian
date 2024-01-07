package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.document
import org.scalajs.dom.html
import tyrian.Cmd

import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.typedarray

/** Given the id of a file input field that has had a file selected, this Cmd will read either raw bytes, an image or
  * text file to return a `Vector[Byte]` or an `HTMLImageElement` or `String` respectively.
  */
object FileReader:

  /** Reads an input file from an input field as base64 encoded image data */
  def readImage[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[String] =
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, d.asInstanceOf[String])
        catch case _ => Result.Error("File is not a base64 string of image data")

    readFromInputField(inputFieldId, ReadType.AsDataUrl)(cast andThen resultToMessage)

  /** Reads an input file as base64 encoded image data */
  def readImage[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[String] =
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, d.asInstanceOf[String])
        catch case _ => Result.Error("File is not a base64 string of image data")

    readFile(file, ReadType.AsDataUrl)(cast andThen resultToMessage)

  /** Reads an input file from an input field as plain text */
  def readText[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[String] =
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, d.asInstanceOf[String])
        catch case _ => Result.Error("File is not text")

    readFromInputField(inputFieldId, ReadType.AsText)(cast andThen resultToMessage)

  /** Reads an input file as plain text */
  def readText[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[String] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[String] =
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, d.asInstanceOf[String])
        catch case _ => Result.Error("File is not text")

    readFile(file, ReadType.AsText)(cast andThen resultToMessage)

  /** Reads an input file from an input field as bytes */
  def readBytes[F[_]: Async, Msg](inputFieldId: String)(resultToMessage: Result[Vector[Byte]] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[Vector[Byte]] =
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, new typedarray.Int8Array(d.asInstanceOf[typedarray.ArrayBuffer]).toVector)
        catch case _ => Result.Error("File is not bytes")

    readFromInputField(inputFieldId, ReadType.AsArrayBuffer)(cast andThen resultToMessage)

  /** Reads an input file as bytes */
  def readBytes[F[_]: Async, Msg](file: dom.File)(resultToMessage: Result[Vector[Byte]] => Msg): Cmd[F, Msg] =
    val cast: Result[js.Any] => Result[Vector[Byte]] =
      case Result.Error(msg) => Result.Error(msg)
      case Result.File(n, p, d) =>
        try Result.File(n, p, new typedarray.Int8Array(d.asInstanceOf[typedarray.ArrayBuffer]).toVector)
        catch case _ => Result.Error("File is not bytes")

    readFile(file, ReadType.AsArrayBuffer)(cast andThen resultToMessage)

  private def readFromInputField[F[_]: Async, Msg](fileInputFieldId: String, readType: ReadType)(
      resultToMessage: Result[js.Any] => Msg
  ): Cmd[F, Msg] =
    val files = document.getElementById(fileInputFieldId).asInstanceOf[html.Input].files
    if files.length == 0 then Cmd.None
    else readFile(files.item(0), readType)(resultToMessage)

  private def readFile[F[_]: Async, Msg](file: dom.File, readAsType: ReadType)(
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
