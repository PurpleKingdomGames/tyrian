package tyrian.ui.elements.stateless.link

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Target
import tyrian.ui.theme.Theme
import tyrian.ui.utils.Lens

// TODO: Styling options via a theme.
final case class Link(
    contents: UIElement[?, ?],
    target: Option[Target],
    url: Option[String], // TODO: URL type? Reuse Location type?
    click: Option[GlobalMsg],
    classNames: Set[String],
    themeOverride: Option[Unit => Unit]
) extends UIElement[Link, Unit]:

  def withContents(newContents: UIElement[?, ?]): Link =
    this.copy(contents = newContents)

  // TODO: Does not work, target is ignored, something about the way routing works, I imagine.
  def withTarget(newTarget: Target): Link =
    this.copy(target = Some(newTarget))
  def removeTarget: Link =
    this.copy(target = None)

  def withUrl(newUrl: String): Link =
    this.copy(url = Some(newUrl))
  def removeUrl: Link =
    this.copy(url = None)

  def onClick(msg: GlobalMsg): Link =
    this.copy(click = Some(msg))
  def removeOnClick: Link =
    this.copy(click = None)

  def withClassNames(classes: Set[String]): Link =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme.Styles, Unit] =
    Lens.unit

  def withThemeOverride(f: Unit => Unit): Link =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Link.View.toHtml(this)

object Link:

  def apply(url: String)(contents: UIElement[?, ?]): Link =
    Link(
      contents = contents,
      target = None,
      url = Some(url),
      click = None,
      classNames = Set(),
      themeOverride = None
    )

  def apply(onClick: GlobalMsg)(contents: UIElement[?, ?]): Link =
    Link(
      contents = contents,
      target = None,
      url = None,
      click = Some(onClick),
      classNames = Set(),
      themeOverride = None
    )

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
          link.click.map(msg => onClick(msg)).getOrElse(EmptyAttribute),
          classAttribute
        )

      a(attributes)(
        link.contents.toElem
      )
