package tyrian.ui

import tyrian.ui.theme.*

final case class Theme(
    colors: ThemeColors,
    fonts: ThemeFonts,
    button: ButtonTheme,
    text: TextTheme
):

  def withColors(colors: ThemeColors): Theme =
    this.copy(colors = colors)

  def withFonts(fonts: ThemeFonts): Theme =
    this.copy(fonts = fonts)

  def withButtonTheme(button: ButtonTheme): Theme =
    this.copy(button = button)

  def withTextTheme(text: TextTheme): Theme =
    this.copy(text = text)

object Theme:

  def default: Theme =
    Theme(
      colors = ThemeColors.default,
      fonts = ThemeFonts.default,
      button = ButtonTheme.default,
      text = TextTheme.default
    )
