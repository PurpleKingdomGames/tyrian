package tyrian.ui.theme

/** Intercept the implicit theme and change it's behaviour for this UI Element.
  */
enum ThemeOverride[ComponentTheme] derives CanEqual:

  /** Do not apply any theme to the UIElement */
  case RemoveTheme()

  /** Do not override, apply the default theme the UIElement */
  case DoNotOverride()

  /** Override the default theme for this UIElement */
  case Override(modify: ComponentTheme => ComponentTheme)

object ThemeOverride:

  inline def NoTheme[ComponentTheme]: ThemeOverride[ComponentTheme] =
    ThemeOverride.RemoveTheme()

  inline def NoOverride[ComponentTheme]: ThemeOverride[ComponentTheme] =
    ThemeOverride.DoNotOverride()
