package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.Extent
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Align
import tyrian.ui.datatypes.Justify
import tyrian.ui.datatypes.Padding
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Container(
    child: UIElement[?, ?],
    padding: Padding,
    justify: Justify,
    align: Align,
    width: Option[Extent],
    height: Option[Extent],
    classNames: Set[String],
    themeOverride: ThemeOverride[ContainerTheme]
) extends UIElement[Container, ContainerTheme]:

  def withPadding(padding: Padding): Container =
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

  def themeLens: Lens[Theme.Default, ContainerTheme] =
    Lens(
      _.container,
      (t, c) => t.copy(container = c)
    )

  def withThemeOverride(value: ThemeOverride[ContainerTheme]): Container =
    this.copy(themeOverride = value)

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Container.toHtml(this)

object Container:

  import tyrian.Html.*
  import tyrian.Style

  def apply(child: UIElement[?, ?]): Container =
    Container(
      child = child,
      padding = Padding.zero,
      justify = Justify.Left,
      align = Align.Top,
      width = None,
      height = None,
      classNames = Set(),
      themeOverride = ThemeOverride.NoOverride
    )

  def toHtml(container: Container)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val baseStyles =
      Style(
        "display"         -> "flex",
        "flex"            -> "1",
        "justify-content" -> container.justify.toCSSValue,
        "align-items"     -> container.align.toCSSValue
      ) |+| container.padding.toStyle

    val containerThemeStyles =
      theme match
        case Theme.None =>
          Style.empty

        case tt: Theme.Default =>
          tt.container.toStyle

    val sizeAttributes = List(
      container.width.map(w => width := w.toCSSValue).toList,
      container.height.map(h => height := h.toCSSValue).toList
    ).flatten

    val classAttribute =
      if container.classNames.isEmpty then EmptyAttribute
      else cls := container.classNames.mkString(" ")

    div(style(baseStyles |+| containerThemeStyles) :: classAttribute :: sizeAttributes)(
      container.child.toElem
    )
