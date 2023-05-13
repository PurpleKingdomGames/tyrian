package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.window

object Nav:

  /** Update the address bar location with a new url. Should be used in conjunction with Tyrian's frontend routing so
    * that when your model decides to change pages, you can update the browser accordingly.
    */
  def pushUrl[F[_]: Async](url: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.pushState("", "", url)
    }

  /** Tells the browser to navigate to a new url. Should be used in conjunction with Tyrian's frontend routing when you
    * detect an external link and wish to follow it.
    */
  def loadUrl[F[_]: Async](href: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.location.href = href
    }
