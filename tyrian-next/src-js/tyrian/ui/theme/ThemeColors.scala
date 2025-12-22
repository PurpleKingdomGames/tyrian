package tyrian.ui.theme

import tyrian.ui.datatypes.RGBA

// TODO: I think these are unused currently.
final case class ThemeColors(
    primary: RGBA,
    secondary: RGBA,
    background: RGBA,
    text: RGBA
)

object ThemeColors:

  def default: ThemeColors =
    ThemeColors(
      primary = RGBA.fromHex("#3366ff"),
      secondary = RGBA.fromHex("#ff3366"),
      background = RGBA.fromHex("#ffffff"),
      text = RGBA.fromHex("#000000")
    )
