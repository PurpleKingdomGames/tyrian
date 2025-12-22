package tyrian.ui.theme

import tyrian.ui.elements.stateful.input.InputTheme
import tyrian.ui.elements.stateless.button.ButtonTheme
import tyrian.ui.elements.stateless.link.LinkTheme
import tyrian.ui.elements.stateless.table.TableTheme
import tyrian.ui.elements.stateless.text.TextThemes
import tyrian.ui.layout.*
import tyrian.ui.theme.*

enum Theme derives CanEqual:
  case None
  case Default(
      config: ThemeConfig,
      elements: ElementThemes
  )

object Theme:

  val default: Theme =
    Theme.Default.default

  object Default:

    val default: Theme.Default =
      Theme.Default(
        ThemeConfig.default,
        ElementThemes.default
      )

    extension (t: Theme.Default)
      // Config

      def withColors(colors: ThemeColors): Theme.Default =
        t.copy(config = t.config.withColors(colors))

      def withFonts(fonts: ThemeFonts): Theme.Default =
        t.copy(config = t.config.withFonts(fonts))

      // Elements

      def withButtonTheme(button: ButtonTheme): Theme.Default =
        t.copy(elements = t.elements.withButtonTheme(button))

      def withCanvasTheme(canvas: ContainerTheme): Theme.Default =
        t.copy(elements = t.elements.withCanvasTheme(canvas))

      def withContainerTheme(container: ContainerTheme): Theme.Default =
        t.copy(elements = t.elements.withContainerTheme(container))

      def withImageTheme(image: ContainerTheme): Theme.Default =
        t.copy(elements = t.elements.withImageTheme(image))

      def withInputTheme(input: InputTheme): Theme.Default =
        t.copy(elements = t.elements.withInputTheme(input))

      def withLinkTheme(link: LinkTheme): Theme.Default =
        t.copy(elements = t.elements.withLinkTheme(link))

      def withTableTheme(table: TableTheme): Theme.Default =
        t.copy(elements = t.elements.withTableTheme(table))

      def withTextTheme(text: TextThemes): Theme.Default =
        t.copy(elements = t.elements.withTextTheme(text))

      def withTextThemes(text: TextThemes): Theme.Default =
        t.copy(elements = t.elements.withTextThemes(text))

  extension (t: Theme)

    // General

    def toOption: Option[Theme.Default] =
      t match
        case Theme.None       => scala.None
        case d: Theme.Default => Some(d)
