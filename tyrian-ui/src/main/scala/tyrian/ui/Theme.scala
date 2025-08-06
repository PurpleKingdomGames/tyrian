package tyrian.ui

import tyrian.ui.button.*
import tyrian.ui.layout.*
import tyrian.ui.text.*
import tyrian.ui.theme.*

final case class Theme(
    button: ButtonTheme,
    colors: ThemeColors,
    fonts: ThemeFonts,
    image: ContainerTheme,
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
      button = ButtonTheme.default,
      colors = ThemeColors.default,
      fonts = ThemeFonts.default,
      image = ContainerTheme.default,
      text = TextThemes.default
    )
