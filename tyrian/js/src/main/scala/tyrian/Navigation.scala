package tyrian

import org.scalajs.dom.HashChangeEvent
import cats.effect.kernel.Async
import org.scalajs.dom.window
import tyrian.Sub

import scala.scalajs.js
import scala.util.control.NonFatal

object Navigation:

  enum Result:
    case HashChange(oldUrl: String, oldFragment: String, newUrl: String, newFragment: String)
    case CurrentHash(hash: String)
    case NoHash

  def onLocationHashChange[Msg](resultToMessage: Result.HashChange => Msg): Sub[Msg] =
    Sub.fromEvent("hashchange", window) { e =>
      try {
        val evt     = e.asInstanceOf[HashChangeEvent]
        val oldFrag = evt.oldURL.substring(evt.oldURL.indexOf("#"))
        val newFrag = evt.newURL.substring(evt.newURL.indexOf("#"))
        Option[Result.HashChange](Result.HashChange(evt.oldURL, oldFrag, evt.newURL, newFrag))
          .map(resultToMessage)
      } catch {
        case NonFatal(e) =>
          None
      }
    }

  def getLocationHash[Msg](resultToMessage: Result => Msg): Cmd[Msg] =
    val task =
      IO.delay {
        val hash = window.location.hash
        if hash.nonEmpty then Result.CurrentHash(hash.substring(1))
        else Result.NoHash
      }
    Cmd.Run(task, resultToMessage)

  def setLocationHash(newHash: String): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      window.location.hash = if newHash.startsWith("#") then newHash else "#" + newHash
    }
