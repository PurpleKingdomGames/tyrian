package tyrian.ui.theme

final case class ThemeFonts(
    body: String,
    heading: String,
    monospace: String
)

object ThemeFonts:

  def default: ThemeFonts =
    ThemeFonts(
      body = "Arial, sans-serif",
      heading = "Georgia, serif",
      monospace = "Courier New, monospace"
    )
