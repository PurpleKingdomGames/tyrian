package tyrian.ui.elements.stateless.text

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
):

  def withNormal(value: TextTheme): TextThemes =
    this.copy(normal = value)
  def modifyNormal(f: TextTheme => TextTheme): TextThemes =
    this.copy(normal = f(normal))

  def withParagraph(value: TextTheme): TextThemes =
    this.copy(paragraph = value)
  def modifyParagraph(f: TextTheme => TextTheme): TextThemes =
    this.copy(paragraph = f(paragraph))

  def withHeading1(value: TextTheme): TextThemes =
    this.copy(heading1 = value)
  def modifyHeading1(f: TextTheme => TextTheme): TextThemes =
    this.copy(heading1 = f(heading1))

  def withHeading2(value: TextTheme): TextThemes =
    this.copy(heading2 = value)
  def modifyHeading2(f: TextTheme => TextTheme): TextThemes =
    this.copy(heading2 = f(heading2))

  def withHeading3(value: TextTheme): TextThemes =
    this.copy(heading3 = value)
  def modifyHeading3(f: TextTheme => TextTheme): TextThemes =
    this.copy(heading3 = f(heading3))

  def withHeading4(value: TextTheme): TextThemes =
    this.copy(heading4 = value)
  def modifyHeading4(f: TextTheme => TextTheme): TextThemes =
    this.copy(heading4 = f(heading4))

  def withHeading5(value: TextTheme): TextThemes =
    this.copy(heading5 = value)
  def modifyHeading5(f: TextTheme => TextTheme): TextThemes =
    this.copy(heading5 = f(heading5))

  def withHeading6(value: TextTheme): TextThemes =
    this.copy(heading6 = value)
  def modifyHeading6(f: TextTheme => TextTheme): TextThemes =
    this.copy(heading6 = f(heading6))

  def withCaption(value: TextTheme): TextThemes =
    this.copy(caption = value)
  def modifyCaption(f: TextTheme => TextTheme): TextThemes =
    this.copy(caption = f(caption))

  def withCode(value: TextTheme): TextThemes =
    this.copy(code = value)
  def modifyCode(f: TextTheme => TextTheme): TextThemes =
    this.copy(code = f(code))

  def withLabel(value: TextTheme): TextThemes =
    this.copy(label = value)
  def modifyLabel(f: TextTheme => TextTheme): TextThemes =
    this.copy(label = f(label))

object TextThemes:

  val normal: TextTheme =
    TextTheme(
      fontSize = FontSize.Medium,
      fontWeight = FontWeight.Normal,
      textColor = RGBA.fromHex("#333333"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Relaxed,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val paragraph: TextTheme =
    TextTheme(
      fontSize = FontSize.Medium,
      fontWeight = FontWeight.Normal,
      textColor = RGBA.fromHex("#333333"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Relaxed,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading1: TextTheme =
    TextTheme(
      fontSize = FontSize.heading1,
      fontWeight = FontWeight.SemiBold,
      textColor = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Tight,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading2: TextTheme =
    TextTheme(
      fontSize = FontSize.heading2,
      fontWeight = FontWeight.SemiBold,
      textColor = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Tight,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading3: TextTheme =
    TextTheme(
      fontSize = FontSize.heading3,
      fontWeight = FontWeight.SemiBold,
      textColor = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Relative(1.3),
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading4: TextTheme =
    TextTheme(
      fontSize = FontSize.heading4,
      fontWeight = FontWeight.SemiBold,
      textColor = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Relative(1.3),
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading5: TextTheme =
    TextTheme(
      fontSize = FontSize.heading5,
      fontWeight = FontWeight.SemiBold,
      textColor = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val heading6: TextTheme =
    TextTheme(
      fontSize = FontSize.heading6,
      fontWeight = FontWeight.SemiBold,
      textColor = RGBA.fromHex("#1a1a1a"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val caption: TextTheme =
    TextTheme(
      fontSize = FontSize.XSmall,
      fontWeight = FontWeight.Normal,
      textColor = RGBA.fromHex("#666666"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = true,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val code: TextTheme =
    TextTheme(
      fontSize = FontSize.Small,
      fontWeight = FontWeight.Normal,
      textColor = RGBA.fromHex("#d73a49"),
      alignment = TextAlignment.Left,
      lineHeight = LineHeight.Normal,
      wrap = false,
      style = TextStyle.Normal,
      decoration = TextDecoration.None
    )

  val label: TextTheme =
    TextTheme(
      fontSize = FontSize.Small,
      fontWeight = FontWeight.Medium,
      textColor = RGBA.fromHex("#333333"),
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
