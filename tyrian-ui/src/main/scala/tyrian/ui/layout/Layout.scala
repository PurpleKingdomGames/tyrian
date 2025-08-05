package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection
import tyrian.ui.datatypes.Ratio
import tyrian.ui.datatypes.SpaceAlignment
import tyrian.ui.datatypes.Spacing

final case class Layout[+Msg](
    direction: LayoutDirection,
    children: List[UIElement[?, Msg]],
    spacing: Spacing,
    spaceAlignment: SpaceAlignment,
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

  def withSpaceAlignment(value: SpaceAlignment): Layout[Msg] =
    this.copy(spaceAlignment = value)

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

  // Aid to memory: "Justify the main axis, align the cross."

  def spaceAround: Layout[Msg] =
    withSpaceAlignment(SpaceAlignment.SpaceAround)

  def spaceBetween: Layout[Msg] =
    withSpaceAlignment(SpaceAlignment.SpaceBetween)

  def spaceEvenly: Layout[Msg] =
    withSpaceAlignment(SpaceAlignment.SpaceEvenly)

  def stretch: Layout[Msg] =
    withSpaceAlignment(SpaceAlignment.Stretch)

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
      spaceAlignment = SpaceAlignment.Stretch,
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
      "justify-content" -> layout.spaceAlignment.toCSSValue,
      "align-items"     -> "stretch",
      "gap"             -> layout.spacing.toCSSValue
    ) |+| layout.direction.toStyle |+| layout.ratio.toStyle

    val classAttribute =
      if layout.classNames.isEmpty then EmptyAttribute
      else cls := layout.classNames.mkString(" ")

    val childrenHtml = layout.children.map(child => child.toHtml(using t))

    div(style(baseStyles), classAttribute)(childrenHtml*)
