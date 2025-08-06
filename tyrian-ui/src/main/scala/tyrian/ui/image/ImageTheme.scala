package tyrian.ui.image

import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.RGBA

final case class ImageTheme(
    border: Option[Border],
    boxShadow: Option[BoxShadow]
):

  def withBorder(border: Border): ImageTheme =
    this.copy(border = Some(border))

  def noBorder: ImageTheme =
    this.copy(border = None)

  def modifyBorder(f: Border => Border): ImageTheme =
    withBorder(
      border match
        case Some(b) => f(b)
        case None    => f(Border.default)
    )

  def solidBorder(width: BorderWidth, color: RGBA): ImageTheme =
    modifyBorder(_.withStyle(BorderStyle.Solid).withWidth(width).withColor(color))
  def dashedBorder(width: BorderWidth, color: RGBA): ImageTheme =
    modifyBorder(_.withStyle(BorderStyle.Dashed).withWidth(width).withColor(color))

  def withBorderRadius(radius: BorderRadius): ImageTheme =
    withBorder(
      border match
        case Some(b) => b.withRadius(radius)
        case None    => Border.default.withRadius(radius)
    )
  def square: ImageTheme       = withBorderRadius(BorderRadius.None)
  def rounded: ImageTheme      = withBorderRadius(BorderRadius.Medium)
  def roundedSmall: ImageTheme = withBorderRadius(BorderRadius.Small)
  def roundedLarge: ImageTheme = withBorderRadius(BorderRadius.Large)
  def circular: ImageTheme     = withBorderRadius(BorderRadius.Full)

  def withBoxShadow(boxShadow: BoxShadow): ImageTheme =
    this.copy(boxShadow = Some(boxShadow))

  def noBoxShadow: ImageTheme =
    this.copy(boxShadow = None)

  def modifyBoxShadow(f: BoxShadow => BoxShadow): ImageTheme =
    withBoxShadow(
      boxShadow match
        case Some(s) => f(s)
        case None    => f(BoxShadow.none)
    )

  def shadowSmall(color: RGBA): ImageTheme =
    withBoxShadow(BoxShadow.small(color))
  def shadowMedium(color: RGBA): ImageTheme =
    withBoxShadow(BoxShadow.medium(color))
  def shadowLarge(color: RGBA): ImageTheme =
    withBoxShadow(BoxShadow.large(color))
  def shadowExtraLarge(color: RGBA): ImageTheme =
    withBoxShadow(BoxShadow.extraLarge(color))

  def toStyle: Style =
    val borderStyle = border.map(_.toStyle).getOrElse(Style.empty)
    val shadowStyle = boxShadow.map(_.toStyle).getOrElse(Style.empty)
    borderStyle |+| shadowStyle

object ImageTheme:

  val default: ImageTheme =
    ImageTheme(
      border = None,
      boxShadow = None
    )
