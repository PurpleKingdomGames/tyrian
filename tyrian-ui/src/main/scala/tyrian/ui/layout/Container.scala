package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.ui.Extent
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Align
import tyrian.ui.datatypes.Justify
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing

final case class Container[+Msg](
    child: UIElement[?, Msg],
    padding: Spacing,
    backgroundColor: Option[RGBA],
    justify: Justify,
    align: Align,
    width: Option[Extent],
    height: Option[Extent],
    // borderRadius: String,
    // border: String,
    // boxShadow: String,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Container[?], Msg]:

  /* TODO: Styling options...
	-	backgroundColor: Color
	-	border: Border e.g., Border(width = 1, color = Color.Black, radius = 4)
	-	shadow: BoxShadow
	-	optional, for depth/elevation
	-	opacity: Double
   */

  def withPadding(padding: Spacing): Container[Msg] =
    this.copy(padding = padding)

  def withBackgroundColor(color: RGBA): Container[Msg] =
    this.copy(backgroundColor = Some(color))

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
    this.copy(_modifyTheme = Some(f))

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
      backgroundColor = None,
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

    val bgColor =
      container.backgroundColor.map(color => Style("background-color" -> color.toCSSValue)).getOrElse(Style.empty)

    val baseStyles = Style(
      "display"         -> "flex",
      "flex"            -> "1",
      "justify-content" -> container.justify.toCSSValue,
      "align-items"     -> container.align.toCSSValue,
      "padding"         -> container.padding.toCSSValue
    ) |+| bgColor

    val sizeAttributes = List(
      container.width.map(w => width := w.toCSSValue).toList,
      container.height.map(h => height := h.toCSSValue).toList
    ).flatten

    val classAttribute =
      if container.classNames.isEmpty then EmptyAttribute
      else cls := container.classNames.mkString(" ")

    val childHtml = container.child.toHtml(using t)

    div(style(baseStyles) :: classAttribute :: sizeAttributes)(childHtml)
