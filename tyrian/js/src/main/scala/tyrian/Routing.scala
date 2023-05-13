package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.window

object Routing:

  /** Provides ultra simple frontend router for convience in minimal use cases.
    *
    * @param internalHRef
    *   A function that converts an internal href (url) to a Msg, i.e. a link to another page on this website.
    * @param externalHRef
    *   A function that converts an external href (url) to a Msg, i.e. a link to a different website.
    */
  def basic[Msg](internalHRef: String => Msg, externalHRef: String => Msg): Location => Msg = {
    case loc @ Location.Internal(_) => internalHRef(loc.href)
    case loc @ Location.External(_) => externalHRef(loc.href)
  }

  /** Provides ultra simple frontend router that ignores internal links to the app's own website, but allows you to
    * follow links to other sites.
    *
    * @param ignore
    *   A user defined 'no-op' Msg that means "ignore this link/href".
    * @param externalHRef
    *   A function that converts an external href (url) to a Msg, i.e. a link to a different website.
    */
  def externalOnly[Msg](ignore: Msg, externalHRef: String => Msg): Location => Msg = {
    case loc @ Location.Internal(_) => ignore
    case loc @ Location.External(_) => externalHRef(loc.href)
  }

  /** Provides a frontend router that ignores and deactivates all links fired by the app. In other words, no `<a href>`
    * style links will work.
    *
    * @param ignore
    *   A user defined 'no-op' Msg that means "ignore this link/href".
    */
  def none[Msg](ignore: Msg): Location => Msg = {
    case loc @ Location.Internal(_) => ignore
    case loc @ Location.External(_) => ignore
  }
