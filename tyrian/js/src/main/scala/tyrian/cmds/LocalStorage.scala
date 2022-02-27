package tyrian.cmds

import org.scalajs.dom
import tyrian.Cmd

import scala.annotation.targetName
import scala.util.control.NonFatal

object LocalStorage:

  enum Error:
    case Failure(message: String)
    case NotFound(key: String)

  def setItem[Msg](key: String, data: String, toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.setItem(key, data))
      catch {
        case NonFatal(e) =>
          observer.onError(Error.Failure(e.getMessage))
      }

      () => ()
    }
  @targetName("setItem_partial")
  def setItem[Msg](key: String, data: String)(toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    setItem(key, data, toMessage)

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def getItem[Msg](key: String, toMessage: Either[Error, String] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        val item = dom.window.localStorage.getItem(key)
        if item == null then observer.onError(Error.NotFound(key))
        else observer.onNext(item)
      catch {
        case NonFatal(e) =>
          observer.onError(Error.Failure(e.getMessage))
      }

      () => ()
    }
  @targetName("getItem_partial")
  def getItem[Msg](key: String)(toMessage: Either[Error, String] => Msg): Cmd[Msg] =
    getItem(key, toMessage)

  def removeItem[Msg](key: String, toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.removeItem(key))
      catch {
        case NonFatal(e) =>
          observer.onError(Error.Failure(e.getMessage))
      }

      () => ()
    }
  @targetName("removeItem_partial")
  def removeItem[Msg](key: String)(toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    removeItem(key, toMessage)

  def clear[Msg](toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.clear())
      catch {
        case NonFatal(e) =>
          observer.onError(Error.Failure(e.getMessage))
      }

      () => ()
    }

  def key[Msg](index: Int, toMessage: Either[Error, String] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.key(index))
      catch {
        case NonFatal(e) =>
          observer.onError(Error.Failure(e.getMessage))
      }

      () => ()
    }
  @targetName("key_partial")
  def key[Msg](index: Int)(toMessage: Either[Error, String] => Msg): Cmd[Msg] =
    key(index, toMessage)

  def length[Msg](toMessage: Either[Error, Int] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.length)
      catch {
        case NonFatal(e) =>
          observer.onError(Error.Failure(e.getMessage))
      }

      () => ()
    }
