package tyrian.ui.theme

import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateless.link.LinkTheme
import tyrian.ui.elements.stateless.text.TextThemes
import tyrian.ui.layout.*
import tyrian.ui.theme.*

enum Theme derives CanEqual:
  case None
  case Default(
      colors: ThemeColors,
      container: ContainerTheme,
      fonts: ThemeFonts,
      image: ContainerTheme,
      input: InputTheme,
      link: LinkTheme,
      text: TextThemes
  )

object Theme:

  val default: Theme =
    Theme.Default.default

  object Default:

    val default: Theme.Default =
      Theme.Default(
        colors = ThemeColors.default,
        container = ContainerTheme.default,
        fonts = ThemeFonts.default,
        image = ContainerTheme.default,
        input = InputTheme.default,
        link = LinkTheme.default,
        text = TextThemes.default
      )

  extension (t: Theme)
    def toOption: Option[Theme.Default] =
      t match
        case Theme.None       => scala.None
        case d: Theme.Default => Some(d)

    def withColors(colors: ThemeColors): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(colors = colors)

    def withFonts(fonts: ThemeFonts): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(fonts = fonts)

    def withContainerTheme(container: ContainerTheme): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(container = container)

    def withInputTheme(input: InputTheme): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(input = input)

    def withLinkTheme(link: LinkTheme): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(link = link)

    def withTextTheme(text: TextThemes): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(text = text)

    def withTextThemes(text: TextThemes): Theme =
      t match
        case None        => t
        case tt: Default => tt.copy(text = text)
