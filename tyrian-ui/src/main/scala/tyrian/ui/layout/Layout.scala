package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.FlexAlignment
import tyrian.ui.datatypes.LayoutDirection
import tyrian.ui.datatypes.Ratio
import tyrian.ui.datatypes.Spacing

/** A vertical layout container using flexbox. */
final case class Layout[+Msg](
    direction: LayoutDirection,
    children: List[UIElement[?, Msg]],
    spacing: Spacing,
    justify: FlexAlignment,
    align: FlexAlignment,
    ratio: Ratio,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Layout[?], Msg]:

  def withDirection(value: LayoutDirection): Layout[Msg] =
    this.copy(direction = value)
  def toRow: Layout[Msg] =
    withDirection(LayoutDirection.Row)
  def toColumn: Layout[Msg] =
    withDirection(LayoutDirection.Column)

  def withSpacing(value: Spacing): Layout[Msg] =
    this.copy(spacing = value)

  def withJustify(value: FlexAlignment): Layout[Msg] =
    this.copy(justify = value)

  def withAlign(value: FlexAlignment): Layout[Msg] =
    this.copy(align = value)

  def withRatio(value: Ratio): Layout[Msg] =
    this.copy(ratio = value)
  // Is this the nicest way to express this?
  def ratio1: Layout[Msg]  = withRatio(Ratio.one)
  def ratio2: Layout[Msg]  = withRatio(Ratio.one)
  def ratio3: Layout[Msg]  = withRatio(Ratio.one)
  def ratio4: Layout[Msg]  = withRatio(Ratio.one)
  def ratio5: Layout[Msg]  = withRatio(Ratio.one)
  def ratio6: Layout[Msg]  = withRatio(Ratio.one)
  def ratio7: Layout[Msg]  = withRatio(Ratio.one)
  def ratio8: Layout[Msg]  = withRatio(Ratio.one)
  def ratio9: Layout[Msg]  = withRatio(Ratio.one)
  def ratio10: Layout[Msg] = withRatio(Ratio.one)

  // TODO: like left center right etc. spaceAround, spaceBetween, spaceEvenly, stretch (default)

  def left: Layout[Msg] =
    direction match
      case LayoutDirection.Row    => withJustify(FlexAlignment.Start)
      case LayoutDirection.Column => withAlign(FlexAlignment.Start)

  def center: Layout[Msg] =
    direction match
      case LayoutDirection.Row    => withJustify(FlexAlignment.Center)
      case LayoutDirection.Column => withAlign(FlexAlignment.Center)

  def right: Layout[Msg] =
    direction match
      case LayoutDirection.Row    => withJustify(FlexAlignment.End)
      case LayoutDirection.Column => withAlign(FlexAlignment.End)

  def top: Layout[Msg] =
    direction match
      case LayoutDirection.Column => withJustify(FlexAlignment.Start)
      case LayoutDirection.Row    => withAlign(FlexAlignment.Start)

  def middle: Layout[Msg] =
    direction match
      case LayoutDirection.Column => withJustify(FlexAlignment.Center)
      case LayoutDirection.Row    => withAlign(FlexAlignment.Center)

  def bottom: Layout[Msg] =
    direction match
      case LayoutDirection.Column => withJustify(FlexAlignment.End)
      case LayoutDirection.Row    => withAlign(FlexAlignment.End)

  def spaceBetween: Layout[Msg] = withJustify(FlexAlignment.SpaceBetween)
  def spaceAround: Layout[Msg]  = withJustify(FlexAlignment.SpaceAround)
  def spaceEvenly: Layout[Msg]  = withJustify(FlexAlignment.SpaceEvenly)

  def withClassNames(classes: Set[String]): Layout[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Layout[Msg] =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Layout.toHtml(this)

object Layout:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.Style

  def apply[Msg](children: UIElement[?, Msg]*): Layout[Msg] =
    Layout(LayoutDirection.Row, children.toList)

  def apply[Msg](direction: LayoutDirection, children: List[UIElement[?, Msg]]): Layout[Msg] =
    Layout(
      direction = direction,
      children = children,
      spacing = Spacing.None,
      justify = FlexAlignment.Start,
      align = FlexAlignment.Start,
      ratio = Ratio.default,
      classNames = Set(),
      _modifyTheme = None
    )

  def apply[Msg](direction: LayoutDirection, children: UIElement[?, Msg]*): Layout[Msg] =
    Layout(direction, children.toList)

  def row[Msg](children: UIElement[?, Msg]*): Layout[Msg] =
    Layout(LayoutDirection.Row, children.toList)

  def column[Msg](children: UIElement[?, Msg]*): Layout[Msg] =
    Layout(LayoutDirection.Column, children.toList)

  def toHtml[Msg](layout: Layout[Msg])(using theme: Theme): Html[Msg] =
    val t = layout._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val baseStyles = Style(
      "display"         -> "flex",
      "justify-content" -> layout.justify.toCSSValue,
      "align-items"     -> layout.align.toCSSValue,
      "gap"             -> layout.spacing.toCSSValue
    ) |+| layout.direction.toStyle |+| layout.ratio.toStyle

    val classAttribute =
      if layout.classNames.isEmpty then EmptyAttribute
      else cls := layout.classNames.mkString(" ")

    val childrenHtml = layout.children.map(child => child.toHtml(using t))

    div(style(baseStyles), classAttribute)(childrenHtml*)
