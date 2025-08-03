package tyrian.ui.text

import tyrian.Style
import tyrian.ui.Theme
import tyrian.ui.datatypes.RGBA

final case class TextTheme(
    fontSize: String,
    fontWeight: String,
    color: RGBA,
    textAlign: String,
    lineHeight: String,
    wrap: Boolean,
    fontStyle: Option[String] = None,
    textDecoration: Option[String] = None
):

  def toStyles(theme: Theme): Style =
    val baseStyle = Style(
      "font-family" -> theme.fonts.body,
      "font-size"   -> fontSize,
      "font-weight" -> fontWeight,
      "color"       -> color.toHexString("#"),
      "text-align"  -> textAlign,
      "line-height" -> lineHeight,
      "white-space" -> (if wrap then "normal" else "nowrap")
    )

    val styleModifiers = List(
      fontStyle.map("font-style" -> _),
      textDecoration.map("text-decoration" -> _)
    ).flatten

    styleModifiers.foldLeft(baseStyle)((style, prop) => style |+| Style(prop))
