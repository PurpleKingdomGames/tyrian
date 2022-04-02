package tyrian.cmds

import cats.effect.kernel.Async
import cats.syntax.bifunctor._
import org.scalajs.dom
import tyrian.Cmd

import scala.annotation.targetName
import scala.util.control.NonFatal

object LocalStorage:

  enum Result:
    case Found(data: String)
    case NotFound(key: String)
    case Key(key: String)
    case Length(length: Int)
    case Success
    case Failure(message: String)

  def setItem[F[_]: Async, Msg](key: String, data: String, toMessage: Result.Success.type => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.setItem(key, data)), _ => toMessage(Result.Success))
  @targetName("setItem_partial")
  def setItem[F[_]: Async, Msg](key: String, data: String)(toMessage: Result => Msg): Cmd[F, Msg] =
    setItem(key, data, toMessage)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def getItem[F[_]: Async, Msg](key: String, toMessage: Either[Result.NotFound, Result.Found] => Msg): Cmd[F, Msg] =
    val f: String => Either[Result.NotFound, Result.Found] = { (s: String) =>
      if s == null then Left(Result.NotFound(key))
      else Right(Result.Found(s))
    }
    val toMsg: String => Msg = f andThen toMessage

    Cmd.Run(Async[F].delay(dom.window.localStorage.getItem(key)), toMsg)
  @targetName("getItem_partial")
  def getItem[F[_]: Async, Msg](key: String)(toMessage: Either[Result.NotFound, Result.Found] => Msg): Cmd[F, Msg] =
    getItem(key, toMessage)

  def removeItem[F[_]: Async, Msg](key: String, toMessage: Result.Success.type => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.removeItem(key)), _ => toMessage(Result.Success))
  @targetName("removeItem_partial")
  def removeItem[F[_]: Async, Msg](key: String)(toMessage: Result => Msg): Cmd[F, Msg] =
    removeItem(key, toMessage)

  def clear[F[_]: Async, Msg](toMessage: Result => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.clear()), _ => toMessage(Result.Success))

  def key[F[_]: Async, Msg](index: Int, toMessage: Result => Msg): Cmd[F, Msg] =
    Cmd.Run(Async[F].delay(dom.window.localStorage.key(index)), s => toMessage(Result.Key(s)))
  @targetName("key_partial")
  def key[F[_]: Async, Msg](index: Int)(toMessage: Result => Msg): Cmd[F, Msg] =
    key(index, toMessage)

  def length[F[_]: Async, Msg](toMessage: Result => Msg): Cmd[F, Msg] =
    val toMsg: Int => Msg = l => toMessage(Result.Length(l))
    Cmd.Run(Async[F].delay(dom.window.localStorage.length), toMsg)
