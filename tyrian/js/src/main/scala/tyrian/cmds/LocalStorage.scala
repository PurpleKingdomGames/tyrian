package tyrian.cmds

import cats.effect.IO
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

  def setItem[Msg](key: String, data: String, toMessage: Result.Success.type => Msg): Cmd[Msg] =
    Cmd.Run(IO(dom.window.localStorage.setItem(key, data)), _ => toMessage(Result.Success))
  @targetName("setItem_partial")
  def setItem[Msg](key: String, data: String)(toMessage: Result => Msg): Cmd[Msg] =
    setItem(key, data, toMessage)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def getItem[Msg](key: String, toMessage: Either[Result.NotFound, Result.Found] => Msg): Cmd[Msg] =
    val f: String => Either[Result.NotFound, Result.Found]= { (s: String) =>
      if s == null then Left(Result.NotFound(key))
      else Right(Result.Found(s))
    }
    val toMsg: String => Msg = f andThen toMessage

    Cmd.Run(IO(dom.window.localStorage.getItem(key)), toMsg)
  @targetName("getItem_partial")
  def getItem[Msg](key: String)(toMessage: Either[Result.NotFound, Result.Found] => Msg): Cmd[Msg] =
    getItem(key, toMessage)

  def removeItem[Msg](key: String, toMessage: Result.Success.type => Msg): Cmd[Msg] =
    Cmd.Run(IO(dom.window.localStorage.removeItem(key)), _ => toMessage(Result.Success))
  @targetName("removeItem_partial")
  def removeItem[Msg](key: String)(toMessage: Result => Msg): Cmd[Msg] =
    removeItem(key, toMessage)

  def clear[Msg](toMessage: Result => Msg): Cmd[Msg] =
    Cmd.Run(IO(dom.window.localStorage.clear()), _ => toMessage(Result.Success))

  def key[Msg](index: Int, toMessage: Result => Msg): Cmd[Msg] =
    Cmd.Run(IO(dom.window.localStorage.key(index)), s => toMessage(Result.Key(s)))
  @targetName("key_partial")
  def key[Msg](index: Int)(toMessage: Result => Msg): Cmd[Msg] =
    key(index, toMessage)

  def length[Msg](toMessage: Result => Msg): Cmd[Msg] =
    val toMsg: Int => Msg = l => toMessage(Result.Length(l))
    Cmd.Run(IO(dom.window.localStorage.length), toMsg)
