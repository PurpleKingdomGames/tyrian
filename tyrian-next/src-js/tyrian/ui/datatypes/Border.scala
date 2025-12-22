package tyrian.ui.datatypes

import tyrian.Style

final case class Border(
    width: BorderWidth,
    style: BorderStyle,
    color: RGBA,
    radius: BorderRadius
):

  def withWidth(newWidth: BorderWidth): Border =
    copy(width = newWidth)

  def withStyle(newStyle: BorderStyle): Border =
    copy(style = newStyle)

  def withColor(newColor: RGBA): Border =
    copy(color = newColor)

  def withRadius(newRadius: BorderRadius): Border =
    copy(radius = newRadius)

  def rounded: Border      = withRadius(BorderRadius.Medium)
  def roundedSmall: Border = withRadius(BorderRadius.Small)
  def roundedLarge: Border = withRadius(BorderRadius.Large)
  def circular: Border     = withRadius(BorderRadius.Full)

  def toStyle: Style =
    Style(
      "border"        -> s"${width.toCSSValue} ${style.toCSSValue} ${color.toHexString("#")}",
      "border-radius" -> radius.toCSSValue
    )

object Border:

  def apply(width: BorderWidth): Border =
    Border(width, BorderStyle.Solid, RGBA.Black, BorderRadius.None)

  def default: Border =
    Border(BorderWidth.default, BorderStyle.default, RGBA.Black, BorderRadius.None)

  val none: Border = Border(BorderWidth.None, BorderStyle.None, RGBA.Black, BorderRadius.None)

  def solid(width: BorderWidth, color: RGBA): Border =
    Border(width, BorderStyle.Solid, color, BorderRadius.None)

  def dashed(width: BorderWidth, color: RGBA): Border =
    Border(width, BorderStyle.Dashed, color, BorderRadius.None)

  def dotted(width: BorderWidth, color: RGBA): Border =
    Border(width, BorderStyle.Dotted, color, BorderRadius.None)

  def thin(color: RGBA): Border   = solid(BorderWidth.Thin, color)
  def medium(color: RGBA): Border = solid(BorderWidth.Medium, color)
  def thick(color: RGBA): Border  = solid(BorderWidth.Thick, color)
