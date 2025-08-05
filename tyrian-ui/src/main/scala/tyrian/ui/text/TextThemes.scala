package tyrian.ui.text

import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.LineHeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.TextAlignment
import tyrian.ui.datatypes.TextDecoration
import tyrian.ui.datatypes.TextStyle

final case class TextThemes(
    normal: TextTheme,
    paragraph: TextTheme,
    heading1: TextTheme,
    heading2: TextTheme,
    heading3: TextTheme,
    heading4: TextTheme,
    heading5: TextTheme,
    heading6: TextTheme,
    caption: TextTheme,
    code: TextTheme,
    label: TextTheme
)

object TextThemes:

  val normal: TextTheme =
    TextTheme(
      fontSize = FontSize.Medium,
      weight = FontWeight.Normal,
      color = RGBA.fromHex("#333333"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Relaxed,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val paragraph: TextTheme =
    TextTheme(
      fontSize = FontSize.Medium,
      weight = FontWeight.Normal,
      color = RGBA.fromHex("#333333"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Relaxed,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading1: TextTheme =
    TextTheme(
      fontSize = FontSize.heading1,
      weight = FontWeight.SemiBold,
      color = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Tight,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading2: TextTheme =
    TextTheme(
      fontSize = FontSize.heading2,
      weight = FontWeight.SemiBold,
      color = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Tight,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading3: TextTheme =
    TextTheme(
      fontSize = FontSize.heading3,
      weight = FontWeight.SemiBold,
      color = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Custom("1.3"),
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading4: TextTheme =
    TextTheme(
      fontSize = FontSize.heading4,
      weight = FontWeight.SemiBold,
      color = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Custom("1.3"),
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading5: TextTheme =
    TextTheme(
      fontSize = FontSize.heading5,
      weight = FontWeight.SemiBold,
      color = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading6: TextTheme =
    TextTheme(
      fontSize = FontSize.heading6,
      weight = FontWeight.SemiBold,
      color = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val caption: TextTheme =
    TextTheme(
      fontSize = FontSize.XSmall,
      weight = FontWeight.Normal,
      color = RGBA.fromHex("#666666"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val code: TextTheme =
    TextTheme(
      fontSize = FontSize.Small,
      weight = FontWeight.Normal,
      color = RGBA.fromHex("#d73a49"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = false,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val label: TextTheme =
    TextTheme(
      fontSize = FontSize.Small,
      weight = FontWeight.Medium,
      color = RGBA.fromHex("#333333"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = false,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val default: TextThemes =
    TextThemes(
      normal = normal,
      paragraph = paragraph,
      heading1 = heading1,
      heading2 = heading2,
      heading3 = heading3,
      heading4 = heading4,
      heading5 = heading5,
      heading6 = heading6,
      caption = caption,
      code = code,
      label = label
    )
