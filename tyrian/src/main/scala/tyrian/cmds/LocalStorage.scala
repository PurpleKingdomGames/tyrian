package tyrian.cmds

import cats.effect.kernel.Async
import org.scalajs.dom
import tyrian.Cmd

import scala.annotation.targetName

object LocalStorage:

  enum Result:
    case Found(data: String)
    case NotFound(key: String)
    case Key(key: String)
    case Length(length: Int)
    case Success
    case Failure(message: String)

  /** save text data to local storage */
  def setItem[F[_]: Async, Msg](key: String, data: String, toMessage: Result.Success.type => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.setItem(key, data)), _ => toMessage(Result.Success))

  /** save text data to local storage */
  @targetName("setItem_partial")
  def setItem[F[_]: Async, Msg](key: String, data: String)(toMessage: Result => Msg): Cmd[F, Msg] =
    setItem(key, data, toMessage)

  /** load text data from local storage */
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def getItem[F[_]: Async, Msg](key: String, toMessage: Either[Result.NotFound, Result.Found] => Msg): Cmd[F, Msg] =
    val f: String => Either[Result.NotFound, Result.Found] = { (s: String) =>
      if s == null then Left(Result.NotFound(key))
      else Right(Result.Found(s))
    }
    val toMsg: String => Msg = f andThen toMessage

    Cmd.Run(Async[F].delay(dom.window.localStorage.getItem(key)), toMsg)

  /** load text data from local storage */
  @targetName("getItem_partial")
  def getItem[F[_]: Async, Msg](key: String)(toMessage: Either[Result.NotFound, Result.Found] => Msg): Cmd[F, Msg] =
    getItem(key, toMessage)

  /** delete a data entry from local storage */
  def removeItem[F[_]: Async, Msg](key: String, toMessage: Result.Success.type => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.removeItem(key)), _ => toMessage(Result.Success))

  /** delete a data entry from local storage */
  @targetName("removeItem_partial")
  def removeItem[F[_]: Async, Msg](key: String)(toMessage: Result => Msg): Cmd[F, Msg] =
    removeItem(key, toMessage)

  /** delete all data entries from local storage */
  def clear[F[_]: Async, Msg](toMessage: Result => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.clear()), _ => toMessage(Result.Success))

  /** Look up a key by index */
  def key[F[_]: Async, Msg](index: Int, toMessage: Result => Msg): Cmd[F, Msg] =
    val toResult: Int => Option[String] => Result = index => {
      case Some(key) => Result.Key(key)
      case None      => Result.NotFound("Key at index: " + index.toString)
    }

    Cmd.Run(Async[F].delay(Option(dom.window.localStorage.key(index))), s => toMessage(toResult(index)(s)))

  /** Look up a key by index */
  @targetName("key_partial")
  def key[F[_]: Async, Msg](index: Int)(toMessage: Result => Msg): Cmd[F, Msg] =
    key(index, toMessage)

  /** Check how many data entries exist */
  def length[F[_]: Async, Msg](toMessage: Result.Length => Msg): Cmd[F, Msg] =
    val toMsg: Int => Msg = l => toMessage(Result.Length(l))
    Cmd.Run(Async[F].delay(dom.window.localStorage.length), toMsg)
