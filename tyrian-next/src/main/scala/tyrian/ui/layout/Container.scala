package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
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

final case class Container(
    child: UIElement[?],
    padding: Spacing,
    justify: Justify,
    align: Align,
    width: Option[Extent],
    height: Option[Extent],
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Container]:

  def withPadding(padding: Spacing): Container =
    this.copy(padding = padding)

  def withJustify(value: Justify): Container =
    this.copy(justify = value)

  def withAlign(value: Align): Container =
    this.copy(align = value)

  def withWidth(width: Extent): Container =
    this.copy(width = Some(width))
  def fillWidth: Container = withWidth(Extent.Fill)

  def withHeight(height: Extent): Container =
    this.copy(height = Some(height))
  def fillHeight: Container = withHeight(Extent.Fill)

  def withSize(width: Extent, height: Extent): Container =
    this.copy(width = Some(width), height = Some(height))
  def fillContainer: Container = withSize(Extent.Fill, Extent.Fill)

  def left: Container =
    withJustify(Justify.Left)

  def center: Container =
    withJustify(Justify.Center)

  def right: Container =
    withJustify(Justify.Right)

  def top: Container =
    withAlign(Align.Top)

  def middle: Container =
    withAlign(Align.Middle)

  def bottom: Container =
    withAlign(Align.Bottom)

  def withClassNames(classes: Set[String]): Container =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Container =
    val h =
      _modifyTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(_modifyTheme = Some(h))

  def modifyContainerTheme(f: ContainerTheme => ContainerTheme): Container =
    val g: Theme => Theme = theme => theme.copy(container = f(theme.container))
    modifyTheme(g)

  def withBorder(border: Border): Container =
    modifyContainerTheme(_.withBorder(border))
  def noBorder: Container =
    modifyContainerTheme(_.noBorder)
  def modifyBorder(f: Border => Border): Container =
    modifyContainerTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Container =
    modifyContainerTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Container =
    modifyContainerTheme(_.dashedBorder(width, color))

  def withBorderRadius(radius: BorderRadius): Container =
    modifyContainerTheme(_.withBorderRadius(radius))

  def square: Container =
    modifyContainerTheme(_.square)
  def rounded: Container =
    modifyContainerTheme(_.rounded)
  def roundedSmall: Container =
    modifyContainerTheme(_.roundedSmall)
  def roundedLarge: Container =
    modifyContainerTheme(_.roundedLarge)
  def circular: Container =
    modifyContainerTheme(_.circular)

  def withBoxShadow(boxShadow: BoxShadow): Container =
    modifyContainerTheme(_.withBoxShadow(boxShadow))
  def noBoxShadow: Container =
    modifyContainerTheme(_.noBoxShadow)
  def modifyBoxShadow(f: BoxShadow => BoxShadow): Container =
    modifyContainerTheme(_.modifyBoxShadow(f))
  def shadowSmall(color: RGBA): Container =
    modifyContainerTheme(_.shadowSmall(color))
  def shadowMedium(color: RGBA): Container =
    modifyContainerTheme(_.shadowMedium(color))
  def shadowLarge(color: RGBA): Container =
    modifyContainerTheme(_.shadowLarge(color))
  def shadowExtraLarge(color: RGBA): Container =
    modifyContainerTheme(_.shadowExtraLarge(color))

  def withOpacity(opacity: Opacity): Container =
    modifyContainerTheme(_.withOpacity(opacity))
  def noOpacity: Container =
    modifyContainerTheme(_.noOpacity)
  def fullyOpaque: Container =
    modifyContainerTheme(_.fullyOpaque)
  def semiTransparent: Container =
    modifyContainerTheme(_.semiTransparent)
  def transparent: Container =
    modifyContainerTheme(_.transparent)

  def withBackgroundColor(color: RGBA): Container =
    modifyContainerTheme(_.withBackgroundColor(color))
  def noBackgroundColor: Container =
    modifyContainerTheme(_.noBackgroundColor)

  def toHtml: Theme ?=> tyrian.Elem[GlobalMsg] =
    Container.toHtml(this)

object Container:

  import tyrian.Html.*
  import tyrian.Style

  def apply(child: UIElement[?]): Container =
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

  def toHtml(container: Container)(using theme: Theme): tyrian.Elem[GlobalMsg] =
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
