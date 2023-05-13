package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.window

object Routing:

  /** Provides ultra simple frontend router for convience in minimal use cases.
    *
    * @param internal
    *   A function that converts an href (url) to a Msg.
    * @param external
    *   A function that converts an href (url) to a Msg.
    */
  def basic[Msg](internal: String => Msg, external: String => Msg): Location => Msg = {
    case loc @ Location.Internal(_) => internal(loc.href)
    case loc @ Location.External(_) => external(loc.href)
  }

  /** Provides ultra simple frontend router for convience in minimal use cases.
    *
    * @param noop
    *   A user defined 'no-op' Msg that means "ignore this link".
    * @param external
    *   A function that converts an href (url) to a Msg.
    */
  def basic[Msg](ignore: Msg, external: String => Msg): Location => Msg = {
    case loc @ Location.Internal(_) => ignore
    case loc @ Location.External(_) => external(loc.href)
  }
