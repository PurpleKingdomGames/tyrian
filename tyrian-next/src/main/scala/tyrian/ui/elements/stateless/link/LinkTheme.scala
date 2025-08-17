package tyrian.ui.elements.stateless.link

import tyrian.Style
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.LineHeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.TextAlignment
import tyrian.ui.datatypes.TextDecoration
import tyrian.ui.datatypes.TextStyle
import tyrian.ui.datatypes.Wrapping
import tyrian.ui.elements.stateless.text.TextTheme
import tyrian.ui.theme.Theme

final case class LinkTheme(
    base: TextTheme,
    hoverColor: Option[RGBA],
    visitedColor: Option[RGBA],
    focusColor: Option[RGBA]
):

  // Delegate basic text styling to the base TextTheme
  def withFontSize(value: FontSize): LinkTheme =
    this.copy(base = base.withFontSize(value))

  def withFontWeight(value: FontWeight): LinkTheme =
    this.copy(base = base.withFontWeight(value))

  def withTextColor(value: RGBA): LinkTheme =
    this.copy(base = base.withTextColor(value))

  def withAlignment(value: TextAlignment): LinkTheme =
    this.copy(base = base.withAlignment(value))

  def withLineHeight(value: LineHeight): LinkTheme =
    this.copy(base = base.withLineHeight(value))

  def withWrapping(value: Wrapping): LinkTheme =
    this.copy(base = base.withWrapping(value))
  def noWrap: LinkTheme =
    withWrapping(Wrapping.NoWrap)
  def wrap: LinkTheme =
    withWrapping(Wrapping.Wrap)

  def withStyle(value: TextStyle): LinkTheme =
    this.copy(base = base.withStyle(value))

  def withDecoration(value: TextDecoration): LinkTheme =
    this.copy(base = base.withDecoration(value))

  // Link-specific styling
  def withHoverColor(color: RGBA): LinkTheme =
    this.copy(hoverColor = Some(color))

  def withVisitedColor(color: RGBA): LinkTheme =
    this.copy(visitedColor = Some(color))

  def withFocusColor(color: RGBA): LinkTheme =
    this.copy(focusColor = Some(color))

  def noHoverColor: LinkTheme =
    this.copy(hoverColor = None)

  def noVisitedColor: LinkTheme =
    this.copy(visitedColor = None)

  def noFocusColor: LinkTheme =
    this.copy(focusColor = None)

  // Convenience methods that delegate to base
  def bold: LinkTheme          = this.copy(base = base.bold)
  def italic: LinkTheme        = this.copy(base = base.italic)
  def underlined: LinkTheme    = this.copy(base = base.underlined)
  def strikethrough: LinkTheme = this.copy(base = base.strikethrough)

  def clearWeight: LinkTheme     = this.copy(base = base.clearWeight)
  def clearStyle: LinkTheme      = this.copy(base = base.clearStyle)
  def clearDecoration: LinkTheme = this.copy(base = base.clearDecoration)

  def alignLeft: LinkTheme    = this.copy(base = base.alignLeft)
  def alignCenter: LinkTheme  = this.copy(base = base.alignCenter)
  def alignRight: LinkTheme   = this.copy(base = base.alignRight)
  def alignJustify: LinkTheme = this.copy(base = base.alignJustify)

  def toStyles(theme: Theme): Style =
    base.toStyles(theme)

  def toHoverStyles: Style =
    hoverColor.map(color => Style("color" -> color.toCSSValue)).getOrElse(Style.empty)

  def toVisitedStyles: Style =
    visitedColor.map(color => Style("color" -> color.toCSSValue)).getOrElse(Style.empty)

  def toFocusStyles: Style =
    focusColor.map(color => Style("color" -> color.toCSSValue)).getOrElse(Style.empty)

object LinkTheme:

  val default: LinkTheme =
    LinkTheme(
      base = TextTheme(
        fontSize = FontSize.Medium,
        fontWeight = FontWeight.Normal,
        textColor = RGBA.fromHex("#0066cc"), // Classic link blue..
        alignment = TextAlignment.Left,
        lineHeight = LineHeight.Normal,
        wrapping = Wrapping.Wrap,
        style = TextStyle.Normal,
        decoration = TextDecoration.Underline // Links are underlined
      ),
      hoverColor = Some(RGBA.fromHex("#004499")),   // Darker blue on hover
      visitedColor = Some(RGBA.fromHex("#551a8b")), // Purple for visited links
      focusColor = Some(RGBA.fromHex("#0066cc"))    // Same as base for focus
    )
