package tyrian.ui.theme

import tyrian.ui.theme.*

enum Theme derives CanEqual:
  case None
  case Default(
      elements: ElementThemes,
      colors: ThemeColors,
      fonts: ThemeFonts
  )

object Theme:

  val default: Theme =
    Theme.Default.default

  object Default:

    val default: Theme.Default =
      Theme.Default(
        ElementThemes.default,
        ThemeColors.default,
        ThemeFonts.default
      )

  extension (t: Theme.None.type)
    def toOption: Option[Theme.Default] =
      scala.None

  extension (t: Theme.Default)
    def toOption: Option[Theme.Default] =
      Some(t)

    def withColors(colors: ThemeColors): Theme.Default =
      t.copy(colors = colors)
    def modifyColors(f: ThemeColors => ThemeColors): Theme.Default =
      withColors(f(t.colors))

    def withFonts(fonts: ThemeFonts): Theme.Default =
      t.copy(fonts = fonts)
    def modifyFonts(f: ThemeFonts => ThemeFonts): Theme.Default =
      withFonts(f(t.fonts))

    def withElementThemes(elements: ElementThemes): Theme.Default =
      t.copy(elements = elements)
    def modifyElementThemes(f: ElementThemes => ElementThemes): Theme.Default =
      withElementThemes(f(t.elements))
