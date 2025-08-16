package tyrian.ui.elements.stateless.link

import tyrian.next.GlobalMsg
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Target
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Link(
    contents: UIElement[?, ?],
    target: Option[Target],
    url: Option[String],
    click: Option[GlobalMsg],
    classNames: Set[String],
    themeOverride: ThemeOverride[LinkTheme]
) extends UIElement[Link, LinkTheme]:

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

  def themeLens: Lens[Theme.Default, LinkTheme] =
    Lens(
      _.link,
      (t, link) => t.copy(link = link)
    )

  def withThemeOverride(value: ThemeOverride[LinkTheme]): Link =
    this.copy(themeOverride = value)

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
      themeOverride = ThemeOverride.NoOverride
    )

  def apply(onClick: GlobalMsg)(contents: UIElement[?, ?]): Link =
    Link(
      contents = contents,
      target = None,
      url = None,
      click = Some(onClick),
      classNames = Set(),
      themeOverride = ThemeOverride.NoOverride
    )

  object View:

    import tyrian.Html.*
    import tyrian.EmptyAttribute

    def toHtml(link: Link): Theme ?=> tyrian.Elem[GlobalMsg] =
      val theme = summon[Theme]

      val linkTheme =
        theme match
          case Theme.None =>
            LinkTheme.default

          case t: Theme.Default =>
            t.link

      val classAttribute =
        if link.classNames.isEmpty then EmptyAttribute
        else cls := link.classNames.mkString(" ")

      val styleAttribute =
        theme match
          case Theme.None       => EmptyAttribute
          case t: Theme.Default => style(linkTheme.toStyles(theme))

      val attributes =
        List(
          link.url.map(u => href := u).getOrElse(EmptyAttribute),
          link.target.map(_.toAttribute).getOrElse(EmptyAttribute),
          link.click.map(msg => onClick(msg)).getOrElse(EmptyAttribute),
          styleAttribute,
          classAttribute
        )

      a(attributes*)(
        link.contents.toElem
      )
