package tyrian.ui.theme

import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateless.text.TextThemes
import tyrian.ui.layout.*
import tyrian.ui.theme.*

enum Theme derives CanEqual:
  case NoStyles
  case Styles(
      colors: ThemeColors,
      container: ContainerTheme,
      fonts: ThemeFonts,
      image: ContainerTheme,
      input: InputTheme,
      text: TextThemes
  )

object Theme:

  val default: Theme =
    Theme.Styles.default

  object Styles:

    val default: Theme.Styles =
      Theme.Styles(
        colors = ThemeColors.default,
        container = ContainerTheme.default,
        fonts = ThemeFonts.default,
        image = ContainerTheme.default,
        input = InputTheme.default,
        text = TextThemes.default
      )

  extension (t: Theme)
    def withColors(colors: ThemeColors): Theme =
      t match
        case NoStyles   => t
        case tt: Styles => tt.copy(colors = colors)

    def withFonts(fonts: ThemeFonts): Theme =
      t match
        case NoStyles   => t
        case tt: Styles => tt.copy(fonts = fonts)

    def withContainerTheme(container: ContainerTheme): Theme =
      t match
        case NoStyles   => t
        case tt: Styles => tt.copy(container = container)

    def withInputTheme(input: InputTheme): Theme =
      t match
        case NoStyles   => t
        case tt: Styles => tt.copy(input = input)

    def withTextTheme(text: TextThemes): Theme =
      t match
        case NoStyles   => t
        case tt: Styles => tt.copy(text = text)

    def withTextThemes(text: TextThemes): Theme =
      t match
        case NoStyles   => t
        case tt: Styles => tt.copy(text = text)
