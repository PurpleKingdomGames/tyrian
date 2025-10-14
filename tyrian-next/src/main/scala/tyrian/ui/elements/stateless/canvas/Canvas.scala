package tyrian.ui.elements.stateless.canvas

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Extent
import tyrian.ui.layout.ContainerTheme
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Canvas(
    width: Option[Extent],
    height: Option[Extent],
    classNames: Set[String],
    id: Option[String],
    themeOverride: ThemeOverride[ContainerTheme]
) extends UIElement[Canvas, ContainerTheme]:

  def withWidth(width: Extent): Canvas =
    this.copy(width = Some(width))
  def fillWidth: Canvas = withWidth(Extent.Fill)

  def withHeight(height: Extent): Canvas =
    this.copy(height = Some(height))
  def fillHeight: Canvas = withHeight(Extent.Fill)

  def withSize(width: Extent, height: Extent): Canvas =
    this.copy(width = Some(width), height = Some(height))
  def fillContainer: Canvas = withSize(Extent.Fill, Extent.Fill)

  def withClassNames(classes: Set[String]): Canvas =
    this.copy(classNames = classes)

  def withId(id: String): Canvas =
    this.copy(id = Some(id))

  def themeLens: Lens[Theme.Default, ContainerTheme] =
    Lens(
      _.image,
      (t, c) => t.copy(canvas = c)
    )

  def withThemeOverride(value: ThemeOverride[ContainerTheme]): Canvas =
    this.copy(themeOverride = value)

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Canvas.toHtml(this)

object Canvas:

  import tyrian.Html.*
  import tyrian.Style

  def apply(): Canvas =
    Canvas(
      width = None,
      height = None,
      classNames = Set.empty,
      id = None,
      themeOverride = ThemeOverride.NoOverride
    )

  def apply(width: Extent, height: Extent): Canvas =
    Canvas(
      width = Some(width),
      height = Some(height),
      classNames = Set.empty,
      id = None,
      themeOverride = ThemeOverride.NoOverride
    )

  def toHtml(c: Canvas)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val sizeAttributes = List(
      c.width.map(w => width := w.toCSSValue).toList,
      c.height.map(h => height := h.toCSSValue).toList
    ).flatten

    val canvasStyles =
      theme match
        case Theme.None =>
          Style.empty

        case tt: Theme.Default =>
          tt.canvas.toStyle

    val styles =
      if canvasStyles.isEmpty then Nil else List(style(canvasStyles))

    val classAttribute =
      if c.classNames.isEmpty then EmptyAttribute
      else cls := c.classNames.mkString(" ")

    val idAttribute =
      c.id.fold(EmptyAttribute)(id.:=.apply)

    val allAttributes =
      sizeAttributes ++ styles ++ List(classAttribute, idAttribute)

    canvas(allAttributes*)()
