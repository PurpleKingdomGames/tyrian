package tyrian.ui.elements.stateless.table

import tyrian.ui.datatypes.RGBA

final case class RowTheme(
    alternative: Option[RGBA],
    background: Option[RGBA]
):

  def withAlternativeBackground(color: RGBA): RowTheme =
    this.copy(alternative = Some(color))
  def noAlternativeBackground: RowTheme =
    this.copy(alternative = None)

  def withBackground(color: RGBA): RowTheme =
    this.copy(background = Some(color))
  def noBackground: RowTheme =
    this.copy(background = None)

object RowTheme:

  val default: RowTheme =
    RowTheme(
      alternative = Some(RGBA.fromHex("#f9f9f9")),
      background = None
    )
