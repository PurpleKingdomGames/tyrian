package tyrian

import org.scalajs.dom.window
import tyrian.Sub
import tyrian.facades.HashChangeEvent

import scala.scalajs.js
import scala.util.control.NonFatal

object Navigation:

  final case class HashChange(oldUrl: String, oldFragment: String, newUrl: String, newFragment: String)
  final case class CurrentHash(hash: String)
  case object NoHash

  def onLocationHashChange[Msg](resultToMessage: HashChange => Msg): Sub[Msg] =
    Sub.fromEvent("hashchange", window) { e =>
      try {
        val evt     = e.asInstanceOf[HashChangeEvent]
        val oldFrag = evt.oldURL.substring(evt.oldURL.indexOf("#"))
        val newFrag = evt.newURL.substring(evt.newURL.indexOf("#"))
        Option(HashChange(evt.oldURL, oldFrag, evt.newURL, newFrag)).map(resultToMessage)
      } catch {
        case NonFatal(e) =>
          None
      }
    }

  def getLocationHash[Msg](resultToMessage: Either[NoHash.type, CurrentHash] => Msg): Cmd[Msg] =
    Cmd
      .Run[NoHash.type, CurrentHash] { observer =>
        val hash = window.location.hash
        if hash.nonEmpty then observer.onNext(CurrentHash(hash.substring(1)))
        else observer.onError(NoHash)
        () => ()
      }
      .attempt(resultToMessage)

  def setLocationHash(newHash: String): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      window.location.hash = if newHash.startsWith("#") then newHash else "#" + newHash
    }
