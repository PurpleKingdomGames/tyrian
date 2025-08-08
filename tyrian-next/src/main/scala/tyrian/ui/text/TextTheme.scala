package tyrian.ui.text

import tyrian.Style
import tyrian.ui.Theme
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.LineHeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.TextAlignment
import tyrian.ui.datatypes.TextDecoration
import tyrian.ui.datatypes.TextStyle

final case class TextTheme(
    fontSize: FontSize,
    weight: FontWeight,
    color: RGBA,
    alignment: TextAlignment,
    lineHeight: LineHeight,
    wrap: Boolean,
    style: TextStyle,
    decoration: TextDecoration
):

  def withFontSize(value: FontSize): TextTheme =
    this.copy(fontSize = value)

  def withWeight(value: FontWeight): TextTheme =
    this.copy(weight = value)

  def withColor(value: RGBA): TextTheme =
    this.copy(color = value)

  def withAlignment(value: TextAlignment): TextTheme =
    this.copy(alignment = value)

  def withLineHeight(value: LineHeight): TextTheme =
    this.copy(lineHeight = value)

  def withWrap(value: Boolean): TextTheme =
    this.copy(wrap = value)
  def wrapText: TextTheme =
    this.copy(wrap = true)
  def noWrap: TextTheme =
    this.copy(wrap = false)

  def withStyle(value: TextStyle): TextTheme =
    this.copy(style = value)

  def withDecoration(value: TextDecoration): TextTheme =
    this.copy(decoration = value)

  def toStyles(theme: Theme): Style =
    val baseStyle = Style(
      "font-family" -> theme.fonts.body,
      "font-size"   -> fontSize.toCSSValue,
      "font-weight" -> weight.toCSSValue,
      "color"       -> color.toCSSValue,
      "text-align"  -> alignment.toCSSValue,
      "line-height" -> lineHeight.toCSSValue,
      "white-space" -> (if wrap then "normal" else "nowrap")
    )

    val styleModifiers = List(
      if style != TextStyle.Normal then Some("font-style" -> style.toCSSValue) else None,
      if decoration != TextDecoration.None then Some("text-decoration" -> decoration.toCSSValue) else None
    ).flatten

    styleModifiers.foldLeft(baseStyle)((style, prop) => style |+| Style(prop))
