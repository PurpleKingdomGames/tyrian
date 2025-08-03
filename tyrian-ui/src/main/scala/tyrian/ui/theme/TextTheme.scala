package tyrian.ui.theme

import tyrian.Style
import tyrian.ui.Theme

final case class TextTheme(
    fontSize: String,
    fontWeight: String,
    color: String,
    textAlign: String,
    lineHeight: String,
    wrap: Boolean
):

  def toStyles(theme: Theme): Style =
    Style(
      "font-family" -> theme.fonts.body,
      "font-size"   -> fontSize,
      "font-weight" -> fontWeight,
      "color"       -> color,
      "text-align"  -> textAlign,
      "white-space" -> "normal",
      "line-height" -> lineHeight
    ) |+|
      (if wrap then Style("white-space" -> "normal")
       else Style("white-space"         -> "preserve nowrap"))

object TextTheme:

  val default: TextTheme =
    TextTheme(
      fontSize = "24px",
      fontWeight = "bold",
      color = "#3366ff",
      textAlign = "center",
      lineHeight = "1.4",
      wrap = false
    )
