package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.HashChangeEvent
import org.scalajs.dom.window
import tyrian.Sub

import scala.scalajs.js

/** Provides simple routing based on url hash (anchor), such as: `http://mysite.com/#page1` */
object Navigation:

  enum Result:
    case HashChange(oldUrl: String, oldFragment: String, newUrl: String, newFragment: String)
    case CurrentHash(hash: String)
    case NoHash

  /** Subscribes to changes in the url hash and reports when they occur */
  def onLocationHashChange[F[_]: Async, Msg](resultToMessage: Result.HashChange => Msg): Sub[F, Msg] =
    Sub.fromEvent("hashchange", window) { e =>
      try {
        val evt     = e.asInstanceOf[HashChangeEvent]
        val oldFrag = evt.oldURL.substring(evt.oldURL.indexOf("#"))
        val newFrag = evt.newURL.substring(evt.newURL.indexOf("#"))
        Option[Result.HashChange](Result.HashChange(evt.oldURL, oldFrag, evt.newURL, newFrag))
          .map(resultToMessage)
      } catch {
        case _ =>
          None
      }
    }

  /** Fetch the current location hash */
  def getLocationHash[F[_]: Async, Msg](resultToMessage: Result => Msg): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        val hash = window.location.hash
        if hash.nonEmpty then Result.CurrentHash(hash.substring(1))
        else Result.NoHash
      }
    Cmd.Run(task, resultToMessage)

  /** Set the location hash, the change can then be detected using the `onLocationHashChange` subscription */
  def setLocationHash[F[_]: Async](newHash: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.location.hash = if newHash.startsWith("#") then newHash else "#" + newHash
    }
