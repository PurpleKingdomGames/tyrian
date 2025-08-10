package tyrian.ui.elements.stateless.link

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Target
import tyrian.ui.theme.Theme

// TODO: Styling options.
final case class Link(
    contents: UIElement[?],
    target: Option[Target],
    url: Option[String], // URL type? Location?
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
) extends UIElement[Link]:

  def withContents(newContents: UIElement[?]): Link =
    this.copy(contents = newContents)

  def withTarget(newTarget: Target): Link = // TODO: Does nothing - something about the routing I guess.
    this.copy(target = Some(newTarget))

  def withUrl(newUrl: String): Link =
    this.copy(url = Some(newUrl))

  def withClassNames(classes: Set[String]): Link =
    this.copy(classNames = classes)

  def withThemeOverride(f: Theme => Theme): Link =
    this.copy(overrideLocalTheme = Some(f))

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Link.View.toHtml(this)

object Link:

  def apply(url: String)(contents: UIElement[?]): Link =
    Link(contents, None, Some(url), Set(), None)

  object View:

    import tyrian.Html.*
    import tyrian.EmptyAttribute

    def toHtml(link: Link): Theme ?=> tyrian.Elem[GlobalMsg] =
      val attributes =
        List(
          link.url.map(u => href := u).getOrElse(EmptyAttribute),
          link.target.map(_.toAttribute).getOrElse(EmptyAttribute)
        )

      a(attributes)(
        link.contents.view
      )
