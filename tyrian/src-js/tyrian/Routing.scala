package tyrian

/** Provides a number of convenience functions to help with routing in simple use-cases. Here the `Location` type is
  * typically hidden away and the user is expected to match on the `String` href (if at all).
  */
object Routing:

  /** Provides a frontend router that treats all links the same way and simply forwards the href to the update function
    * to be dealt with there. Does not differentiate between internal and external links.
    *
    * @param internalHRef
    *   A function that converts an internal href (url) to a Msg, i.e. a link to another page on this website.
    * @param externalHRef
    *   A function that converts an external href (url) to a Msg, i.e. a link to a different website.
    */
  def all[Msg](forward: String => Msg): Location => Msg = {
    case loc @ Location.Internal(_) => forward(loc.href)
    case loc @ Location.External(_) => forward(loc.href)
  }

  /** Provides ultra simple frontend router based on string matching for convience in minimal use cases.
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

  /** Provides a simple frontend router that ignores internal links to the app's own website (i.e. your app really is a
    * single page app), but allows you to follow external links to other sites.
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
    * style links will work. Does not differentiate between internal and external links.
    *
    * @param ignore
    *   A user defined 'no-op' Msg that means "ignore this link/href".
    */
  def none[Msg](ignore: Msg): Location => Msg = {
    case loc @ Location.Internal(_) => ignore
    case loc @ Location.External(_) => ignore
  }
