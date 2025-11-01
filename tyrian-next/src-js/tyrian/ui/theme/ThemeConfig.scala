package tyrian.ui.theme

/** ThemeConfig is all the default / fallback values that will be used as a base line for the current theme.
  */
final case class ThemeConfig(
    colors: ThemeColors,
    fonts: ThemeFonts
):

  def withColors(colors: ThemeColors): ThemeConfig =
    this.copy(colors = colors)

  def withFonts(fonts: ThemeFonts): ThemeConfig =
    this.copy(fonts = fonts)

object ThemeConfig:

  val default: ThemeConfig =
    ThemeConfig(
      ThemeColors.default,
      ThemeFonts.default
    )
