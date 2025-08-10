package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.Extent
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Align
import tyrian.ui.datatypes.BackgroundMode
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.Fill
import tyrian.ui.datatypes.Justify
import tyrian.ui.datatypes.Opacity
import tyrian.ui.datatypes.Position
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme

final case class Container(
    child: UIElement[?],
    padding: Spacing,
    justify: Justify,
    align: Align,
    width: Option[Extent],
    height: Option[Extent],
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
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

  def withThemeOverride(f: Theme => Theme): Container =
    val h =
      overrideLocalTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(overrideLocalTheme = Some(h))

  def overrideContainerTheme(f: ContainerTheme => ContainerTheme): Container =
    val g: Theme => Theme = theme => theme.copy(container = f(theme.container))
    withThemeOverride(g)

  def withBorder(border: Border): Container =
    overrideContainerTheme(_.withBorder(border))
  def noBorder: Container =
    overrideContainerTheme(_.noBorder)
  def modifyBorder(f: Border => Border): Container =
    overrideContainerTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Container =
    overrideContainerTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Container =
    overrideContainerTheme(_.dashedBorder(width, color))

  def withBorderRadius(radius: BorderRadius): Container =
    overrideContainerTheme(_.withBorderRadius(radius))

  def square: Container =
    overrideContainerTheme(_.square)
  def rounded: Container =
    overrideContainerTheme(_.rounded)
  def roundedSmall: Container =
    overrideContainerTheme(_.roundedSmall)
  def roundedLarge: Container =
    overrideContainerTheme(_.roundedLarge)
  def circular: Container =
    overrideContainerTheme(_.circular)

  def withBoxShadow(boxShadow: BoxShadow): Container =
    overrideContainerTheme(_.withBoxShadow(boxShadow))
  def noBoxShadow: Container =
    overrideContainerTheme(_.noBoxShadow)
  def modifyBoxShadow(f: BoxShadow => BoxShadow): Container =
    overrideContainerTheme(_.modifyBoxShadow(f))
  def shadowSmall(color: RGBA): Container =
    overrideContainerTheme(_.shadowSmall(color))
  def shadowMedium(color: RGBA): Container =
    overrideContainerTheme(_.shadowMedium(color))
  def shadowLarge(color: RGBA): Container =
    overrideContainerTheme(_.shadowLarge(color))
  def shadowExtraLarge(color: RGBA): Container =
    overrideContainerTheme(_.shadowExtraLarge(color))

  def withOpacity(opacity: Opacity): Container =
    overrideContainerTheme(_.withOpacity(opacity))
  def noOpacity: Container =
    overrideContainerTheme(_.noOpacity)
  def fullyOpaque: Container =
    overrideContainerTheme(_.fullyOpaque)
  def semiTransparent: Container =
    overrideContainerTheme(_.semiTransparent)
  def transparent: Container =
    overrideContainerTheme(_.transparent)

  def withBackgroundColor(color: RGBA): Container =
    overrideContainerTheme(_.withBackgroundColor(color))
  def noBackground: Container =
    overrideContainerTheme(_.noBackground)

  def withBackgroundFill(fill: Fill): Container =
    overrideContainerTheme(_.withBackgroundFill(fill))

  def withBackgroundImage(url: String): Container =
    withBackgroundFill(Fill.Image(url))
  def withBackgroundImageAt(url: String, position: Position): Container =
    withBackgroundFill(Fill.Image(url).withPosition(position))
  def withBackgroundImageCover(url: String): Container =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.coverNoRepeat))
  def withBackgroundImageContain(url: String): Container =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.containNoRepeat))
  def withBackgroundImageFill(url: String): Container =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.fillNoRepeat))
  def withBackgroundImageTiled(url: String): Container =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.autoRepeat))
  def withBackgroundImageRepeatX(url: String): Container =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.autoRepeatX))
  def withBackgroundImageRepeatY(url: String): Container =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.autoRepeatY))

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
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
      overrideLocalTheme = None
    )

  def toHtml(container: Container)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val t = container.overrideLocalTheme match
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

    val childHtml = container.child.view(using t)

    div(style(baseStyles |+| containerThemeStyles) :: classAttribute :: sizeAttributes)(childHtml)
