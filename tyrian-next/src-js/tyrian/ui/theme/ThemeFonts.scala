package tyrian.ui.theme

import tyrian.ui.datatypes.FontFamily

final case class ThemeFonts(
    body: FontFamily,
    heading: FontFamily,
    monospace: FontFamily
)

object ThemeFonts:

  def default: ThemeFonts =
    ThemeFonts(
      body = FontFamily.sansSerif,
      heading = FontFamily.serif,
      monospace = FontFamily.monospace
    )
