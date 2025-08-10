package tyrian.ui.elements.stateless.link

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Target
import tyrian.ui.theme.Theme
import tyrian.ui.utils.Lens

// TODO: Styling options via a theme.
// TODO: Missing onClick behaviour?
final case class Link(
    contents: UIElement[?, ?],
    target: Option[Target],
    url: Option[String], // TODO: URL type? Reuse Location type?
    classNames: Set[String],
    themeOverride: Option[Unit => Unit]
) extends UIElement[Link, Unit]:

  def withContents(newContents: UIElement[?, ?]): Link =
    this.copy(contents = newContents)

  // TODO: Does not work, target is ignored, something about the way routing works, I imagine.
  def withTarget(newTarget: Target): Link =
    this.copy(target = Some(newTarget))

  def withUrl(newUrl: String): Link =
    this.copy(url = Some(newUrl))

  def withClassNames(classes: Set[String]): Link =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme, Unit] =
    Lens.unit

  def withThemeOverride(f: Unit => Unit): Link =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Link.View.toHtml(this)

object Link:

  def apply(url: String)(contents: UIElement[?, ?]): Link =
    Link(contents, None, Some(url), Set(), None)

  object View:

    import tyrian.Html.*
    import tyrian.EmptyAttribute

    def toHtml(link: Link): Theme ?=> tyrian.Elem[GlobalMsg] =
      val classAttribute =
        if link.classNames.isEmpty then EmptyAttribute
        else cls := link.classNames.mkString(" ")

      val attributes =
        List(
          link.url.map(u => href := u).getOrElse(EmptyAttribute),
          link.target.map(_.toAttribute).getOrElse(EmptyAttribute),
          classAttribute
        )

      a(attributes)(
        link.contents.toElem
      )
