package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.window

/** The `Nav` object provides `Cmd`s that are mainly expected to be used in conjunction with Tyrian's frontend routing.
  * It exposes some of the functions from the JS `Location` and `History` apis.
  */
object Nav:

  /** Move back one in the browser's history.
    */
  def back[F[_]: Async]: Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.back()
    }

  /** Move forward one in the browser's history.
    */
  def forward[F[_]: Async]: Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.forward()
    }

  /** Tells the browser to navigate to a new url. Should be used in conjunction with Tyrian's frontend routing when you
    * detect an external link and wish to follow it.
    */
  def loadUrl[F[_]: Async](href: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.location.href = href
    }

  /** Open's the link in a new tab / window, using the `_blank` target.
    */
  def openUrl[F[_]: Async](href: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.open(href, "_blank")
    }

  /** Update the address bar location with a new url. Should be used in conjunction with Tyrian's frontend routing so
    * that when your model decides to change pages, you can update the browser accordingly.
    */
  def pushUrl[F[_]: Async](url: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.pushState("", "", url)
    }
