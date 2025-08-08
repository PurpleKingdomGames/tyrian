package tyrian.ui.link

import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Target

// TODO: Styling options.
final case class Link[+Msg](
    contents: UIElement[?, Msg],
    target: Option[Target],
    url: Option[String], // URL type? Location?
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Link[?], Msg]:

  def withContents[LubMsg >: Msg](newContents: UIElement[?, LubMsg]): Link[LubMsg] =
    this.copy(contents = newContents)

  def withTarget(newTarget: Target): Link[Msg] = // TODO: Does nothing - something about the routing I guess.
    this.copy(target = Some(newTarget))

  def withUrl(newUrl: String): Link[Msg] =
    this.copy(url = Some(newUrl))

  def withClassNames(classes: Set[String]): Link[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Link[Msg] =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Link.View.toHtml(this)

object Link:

  def apply[Msg](url: String)(contents: UIElement[?, Msg]): Link[Msg] =
    Link(contents, None, Some(url), Set(), None)

  object View:

    import tyrian.Html.*
    import tyrian.EmptyAttribute

    def toHtml[Msg](link: Link[Msg]): Theme ?=> tyrian.Html[Msg] =
      val attributes =
        List(
          link.url.map(u => href := u).getOrElse(EmptyAttribute),
          link.target.map(_.toAttribute).getOrElse(EmptyAttribute)
        )

      a(attributes)(
        link.contents.toHtml
      )
