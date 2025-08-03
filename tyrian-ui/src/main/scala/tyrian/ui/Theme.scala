package tyrian.ui

import tyrian.ui.button.*
import tyrian.ui.text.*
import tyrian.ui.theme.*

final case class Theme(
    colors: ThemeColors,
    fonts: ThemeFonts,
    button: ButtonTheme,
    text: TextThemes
):

  def withColors(colors: ThemeColors): Theme =
    this.copy(colors = colors)

  def withFonts(fonts: ThemeFonts): Theme =
    this.copy(fonts = fonts)

  def withButtonTheme(button: ButtonTheme): Theme =
    this.copy(button = button)

  def withTextTheme(text: TextThemes): Theme =
    this.copy(text = text)

  def withTextThemes(text: TextThemes): Theme =
    this.copy(text = text)

object Theme:

  def default: Theme =
    Theme(
      colors = ThemeColors.default,
      fonts = ThemeFonts.default,
      button = ButtonTheme.default,
      text = TextThemes.default
    )
