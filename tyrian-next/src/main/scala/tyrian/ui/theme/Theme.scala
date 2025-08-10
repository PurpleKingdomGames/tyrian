package tyrian.ui.theme

import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateless.text.TextThemes
import tyrian.ui.layout.*
import tyrian.ui.theme.*

final case class Theme(
    colors: ThemeColors,
    container: ContainerTheme,
    fonts: ThemeFonts,
    image: ContainerTheme,
    input: InputTheme,
    text: TextThemes
):

  def withColors(colors: ThemeColors): Theme =
    this.copy(colors = colors)

  def withFonts(fonts: ThemeFonts): Theme =
    this.copy(fonts = fonts)

  def withContainerTheme(container: ContainerTheme): Theme =
    this.copy(container = container)

  def withInputTheme(input: InputTheme): Theme =
    this.copy(input = input)

  def withTextTheme(text: TextThemes): Theme =
    this.copy(text = text)

  def withTextThemes(text: TextThemes): Theme =
    this.copy(text = text)

object Theme:

  def default: Theme =
    Theme(
      colors = ThemeColors.default,
      container = ContainerTheme.default,
      fonts = ThemeFonts.default,
      image = ContainerTheme.default,
      input = InputTheme.default,
      text = TextThemes.default
    )
