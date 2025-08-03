package tyrian.ui.text

import tyrian.ui.datatypes.RGBA

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
      fontSize = "16px",
      fontWeight = "400",
      color = RGBA.fromHexString("#333333"),
      textAlign = "left",
      lineHeight = "1.5",
      wrap = true
    )

  val paragraph: TextTheme =
    TextTheme(
      fontSize = "16px",
      fontWeight = "400",
      color = RGBA.fromHexString("#333333"),
      textAlign = "left",
      lineHeight = "1.5",
      wrap = true
    )

  val heading1: TextTheme =
    TextTheme(
      fontSize = "32px",
      fontWeight = "600",
      color = RGBA.fromHexString("#1a1a1a"),
      textAlign = "left",
      lineHeight = "1.2",
      wrap = true
    )

  val heading2: TextTheme =
    TextTheme(
      fontSize = "28px",
      fontWeight = "600",
      color = RGBA.fromHexString("#1a1a1a"),
      textAlign = "left",
      lineHeight = "1.2",
      wrap = true
    )

  val heading3: TextTheme =
    TextTheme(
      fontSize = "24px",
      fontWeight = "600",
      color = RGBA.fromHexString("#1a1a1a"),
      textAlign = "left",
      lineHeight = "1.3",
      wrap = true
    )

  val heading4: TextTheme =
    TextTheme(
      fontSize = "20px",
      fontWeight = "600",
      color = RGBA.fromHexString("#1a1a1a"),
      textAlign = "left",
      lineHeight = "1.3",
      wrap = true
    )

  val heading5: TextTheme =
    TextTheme(
      fontSize = "18px",
      fontWeight = "600",
      color = RGBA.fromHexString("#1a1a1a"),
      textAlign = "left",
      lineHeight = "1.4",
      wrap = true
    )

  val heading6: TextTheme =
    TextTheme(
      fontSize = "16px",
      fontWeight = "600",
      color = RGBA.fromHexString("#1a1a1a"),
      textAlign = "left",
      lineHeight = "1.4",
      wrap = true
    )

  val caption: TextTheme =
    TextTheme(
      fontSize = "12px",
      fontWeight = "400",
      color = RGBA.fromHexString("#666666"),
      textAlign = "left",
      lineHeight = "1.4",
      wrap = true
    )

  val code: TextTheme =
    TextTheme(
      fontSize = "14px",
      fontWeight = "400",
      color = RGBA.fromHexString("#d73a49"),
      textAlign = "left",
      lineHeight = "1.4",
      wrap = false
    )

  val label: TextTheme =
    TextTheme(
      fontSize = "14px",
      fontWeight = "500",
      color = RGBA.fromHexString("#333333"),
      textAlign = "left",
      lineHeight = "1.4",
      wrap = false
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
