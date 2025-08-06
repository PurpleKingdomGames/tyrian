package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.ui.Extent
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Align
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.Justify
import tyrian.ui.datatypes.Opacity
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing

final case class Container[+Msg](
    child: UIElement[?, Msg],
    padding: Spacing,
    justify: Justify,
    align: Align,
    width: Option[Extent],
    height: Option[Extent],
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Container[?], Msg]:

  def withPadding(padding: Spacing): Container[Msg] =
    this.copy(padding = padding)

  def withJustify(value: Justify): Container[Msg] =
    this.copy(justify = value)

  def withAlign(value: Align): Container[Msg] =
    this.copy(align = value)

  def withWidth(width: Extent): Container[Msg] =
    this.copy(width = Some(width))
  def fillWidth: Container[Msg] = withWidth(Extent.Fill)

  def withHeight(height: Extent): Container[Msg] =
    this.copy(height = Some(height))
  def fillHeight: Container[Msg] = withHeight(Extent.Fill)

  def withSize(width: Extent, height: Extent): Container[Msg] =
    this.copy(width = Some(width), height = Some(height))
  def fillContainer: Container[Msg] = withSize(Extent.Fill, Extent.Fill)

  def left: Container[Msg] =
    withJustify(Justify.Left)

  def center: Container[Msg] =
    withJustify(Justify.Center)

  def right: Container[Msg] =
    withJustify(Justify.Right)

  def top: Container[Msg] =
    withAlign(Align.Top)

  def middle: Container[Msg] =
    withAlign(Align.Middle)

  def bottom: Container[Msg] =
    withAlign(Align.Bottom)

  def withClassNames(classes: Set[String]): Container[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Container[Msg] =
    val h =
      _modifyTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(_modifyTheme = Some(h))

  def modifyContainerTheme(f: ContainerTheme => ContainerTheme): Container[Msg] =
    val g: Theme => Theme = theme => theme.copy(container = f(theme.container))
    modifyTheme(g)

  def withBorder(border: Border): Container[Msg] =
    modifyContainerTheme(_.withBorder(border))
  def noBorder: Container[Msg] =
    modifyContainerTheme(_.noBorder)
  def modifyBorder(f: Border => Border): Container[Msg] =
    modifyContainerTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Container[Msg] =
    modifyContainerTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Container[Msg] =
    modifyContainerTheme(_.dashedBorder(width, color))

  def withBorderRadius(radius: BorderRadius): Container[Msg] =
    modifyContainerTheme(_.withBorderRadius(radius))

  def square: Container[Msg] =
    modifyContainerTheme(_.square)
  def rounded: Container[Msg] =
    modifyContainerTheme(_.rounded)
  def roundedSmall: Container[Msg] =
    modifyContainerTheme(_.roundedSmall)
  def roundedLarge: Container[Msg] =
    modifyContainerTheme(_.roundedLarge)
  def circular: Container[Msg] =
    modifyContainerTheme(_.circular)

  def withBoxShadow(boxShadow: BoxShadow): Container[Msg] =
    modifyContainerTheme(_.withBoxShadow(boxShadow))
  def noBoxShadow: Container[Msg] =
    modifyContainerTheme(_.noBoxShadow)
  def modifyBoxShadow(f: BoxShadow => BoxShadow): Container[Msg] =
    modifyContainerTheme(_.modifyBoxShadow(f))
  def shadowSmall(color: RGBA): Container[Msg] =
    modifyContainerTheme(_.shadowSmall(color))
  def shadowMedium(color: RGBA): Container[Msg] =
    modifyContainerTheme(_.shadowMedium(color))
  def shadowLarge(color: RGBA): Container[Msg] =
    modifyContainerTheme(_.shadowLarge(color))
  def shadowExtraLarge(color: RGBA): Container[Msg] =
    modifyContainerTheme(_.shadowExtraLarge(color))

  def withOpacity(opacity: Opacity): Container[Msg] =
    modifyContainerTheme(_.withOpacity(opacity))
  def noOpacity: Container[Msg] =
    modifyContainerTheme(_.noOpacity)
  def fullyOpaque: Container[Msg] =
    modifyContainerTheme(_.fullyOpaque)
  def semiTransparent: Container[Msg] =
    modifyContainerTheme(_.semiTransparent)
  def transparent: Container[Msg] =
    modifyContainerTheme(_.transparent)

  def withBackgroundColor(color: RGBA): Container[Msg] =
    modifyContainerTheme(_.withBackgroundColor(color))
  def noBackgroundColor: Container[Msg] =
    modifyContainerTheme(_.noBackgroundColor)

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Container.toHtml(this)

object Container:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.Style

  def apply[Msg](child: UIElement[?, Msg]): Container[Msg] =
    Container(
      child = child,
      padding = Spacing.None,
      justify = Justify.Left,
      align = Align.Top,
      width = None,
      height = None,
      classNames = Set(),
      _modifyTheme = None
    )

  def toHtml[Msg](container: Container[Msg])(using theme: Theme): Html[Msg] =
    val t = container._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val baseStyles = Style(
      "display"         -> "flex",
      "flex"            -> "1",
      "justify-content" -> container.justify.toCSSValue,
      "align-items"     -> container.align.toCSSValue,
      "padding"         -> container.padding.toCSSValue
    )

    val containerThemeStyles = t.container.toStyle

    val sizeAttributes = List(
      container.width.map(w => width := w.toCSSValue).toList,
      container.height.map(h => height := h.toCSSValue).toList
    ).flatten

    val classAttribute =
      if container.classNames.isEmpty then EmptyAttribute
      else cls := container.classNames.mkString(" ")

    val childHtml = container.child.toHtml(using t)

    div(style(baseStyles |+| containerThemeStyles) :: classAttribute :: sizeAttributes)(childHtml)
