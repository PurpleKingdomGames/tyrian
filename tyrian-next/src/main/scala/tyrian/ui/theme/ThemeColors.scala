package tyrian.ui.theme

// TODO: Should all use the RGBA type.
final case class ThemeColors(
    primary: String,
    secondary: String,
    background: String,
    text: String
)

object ThemeColors:

  def default: ThemeColors =
    ThemeColors(
      primary = "#3366ff",
      secondary = "#ff3366",
      background = "#ffffff",
      text = "#000000"
    )
