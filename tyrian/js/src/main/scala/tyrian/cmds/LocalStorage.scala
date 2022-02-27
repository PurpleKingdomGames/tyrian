package tyrian.cmds

import org.scalajs.dom
import tyrian.Cmd

import scala.annotation.targetName
import scala.util.control.NonFatal

object LocalStorage:

  final case class Error(message: String)

  def setItem[Msg](key: String, data: String, toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.setItem(key, data))
      catch {
        case NonFatal(e) =>
          observer.onError(Error(e.getMessage))
      }

      () => ()
    }
  @targetName("setItem_partial")
  def setItem[Msg](key: String, data: String)(toMessage: Either[Error, Unit] => Msg): Cmd[Msg] =
    setItem(key, data, toMessage)

  def getItem[Msg](key: String, toMessage: Either[Error, String] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.getItem(key))
      catch {
        case NonFatal(e) =>
          observer.onError(Error(e.getMessage))
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
          observer.onError(Error(e.getMessage))
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
          observer.onError(Error(e.getMessage))
      }

      () => ()
    }

  def key[Msg](index: Int, toMessage: Either[Error, String] => Msg): Cmd[Msg] =
    Cmd.Run(toMessage) { observer =>
      try
        observer.onNext(dom.window.localStorage.key(index))
      catch {
        case NonFatal(e) =>
          observer.onError(Error(e.getMessage))
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
          observer.onError(Error(e.getMessage))
      }

      () => ()
    }
