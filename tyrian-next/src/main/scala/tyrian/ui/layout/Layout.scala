package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection
import tyrian.ui.datatypes.Ratio
import tyrian.ui.datatypes.SpaceAlignment
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme
import tyrian.ui.utils.Lens

final case class Layout(
    direction: LayoutDirection,
    children: List[UIElement[?, ?]],
    spacing: Spacing,
    spaceAlignment: SpaceAlignment,
    ratio: Ratio,
    classNames: Set[String],
    themeOverride: Option[Unit => Unit]
) extends UIElement[Layout, Unit]:

  def withDirection(value: LayoutDirection): Layout =
    this.copy(direction = value)
  def toRow: Layout =
    withDirection(LayoutDirection.Row)
  def toColumn: Layout =
    withDirection(LayoutDirection.Column)

  def withSpacing(value: Spacing): Layout =
    this.copy(spacing = value)

  def withSpaceAlignment(value: SpaceAlignment): Layout =
    this.copy(spaceAlignment = value)

  def withRatio(value: Ratio): Layout =
    this.copy(ratio = value)
  // Is this the nicest way to express this?
  def ratio1: Layout  = withRatio(Ratio.one)
  def ratio2: Layout  = withRatio(Ratio.one)
  def ratio3: Layout  = withRatio(Ratio.one)
  def ratio4: Layout  = withRatio(Ratio.one)
  def ratio5: Layout  = withRatio(Ratio.one)
  def ratio6: Layout  = withRatio(Ratio.one)
  def ratio7: Layout  = withRatio(Ratio.one)
  def ratio8: Layout  = withRatio(Ratio.one)
  def ratio9: Layout  = withRatio(Ratio.one)
  def ratio10: Layout = withRatio(Ratio.one)

  // Aid to memory: "Justify the main axis, align the cross."

  def spaceAround: Layout =
    withSpaceAlignment(SpaceAlignment.SpaceAround)

  def spaceBetween: Layout =
    withSpaceAlignment(SpaceAlignment.SpaceBetween)

  def spaceEvenly: Layout =
    withSpaceAlignment(SpaceAlignment.SpaceEvenly)

  def stretch: Layout =
    withSpaceAlignment(SpaceAlignment.Stretch)

  def withClassNames(classes: Set[String]): Layout =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme, Unit] =
    Lens.unit

  def withThemeOverride(f: Unit => Unit): Layout =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Layout.toHtml(this)

object Layout:

  import tyrian.Html.*
  import tyrian.Style

  def apply(children: UIElement[?, ?]*): Layout =
    Layout(LayoutDirection.Row, children.toList)

  def apply(direction: LayoutDirection, children: List[UIElement[?, ?]]): Layout =
    Layout(
      direction = direction,
      children = children,
      spacing = Spacing.None,
      spaceAlignment = SpaceAlignment.Stretch,
      ratio = Ratio.default,
      classNames = Set(),
      themeOverride = None
    )

  def apply(direction: LayoutDirection, children: UIElement[?, ?]*): Layout =
    Layout(direction, children.toList)

  def row(children: UIElement[?, ?]*): Layout =
    Layout(LayoutDirection.Row, children.toList)

  def column(children: UIElement[?, ?]*): Layout =
    Layout(LayoutDirection.Column, children.toList)

  def toHtml(layout: Layout)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val baseStyles = Style(
      "display"         -> "flex",
      "justify-content" -> layout.spaceAlignment.toCSSValue,
      "align-items"     -> "stretch",
      "gap"             -> layout.spacing.toCSSValue
    ) |+| layout.direction.toStyle |+| layout.ratio.toStyle

    val classAttribute =
      if layout.classNames.isEmpty then EmptyAttribute
      else cls := layout.classNames.mkString(" ")

    val childrenHtml = layout.children.map(_.toElem)

    div(style(baseStyles), classAttribute)(childrenHtml*)
