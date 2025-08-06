package tyrian.ui.layout

import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.RGBA

final case class ContainerTheme(
    border: Option[Border],
    boxShadow: Option[BoxShadow]
):

  def withBorder(border: Border): ContainerTheme =
    this.copy(border = Some(border))

  def noBorder: ContainerTheme =
    this.copy(border = None)

  def modifyBorder(f: Border => Border): ContainerTheme =
    withBorder(
      border match
        case Some(b) => f(b)
        case None    => f(Border.default)
    )

  def solidBorder(width: BorderWidth, color: RGBA): ContainerTheme =
    modifyBorder(_.withStyle(BorderStyle.Solid).withWidth(width).withColor(color))
  def dashedBorder(width: BorderWidth, color: RGBA): ContainerTheme =
    modifyBorder(_.withStyle(BorderStyle.Dashed).withWidth(width).withColor(color))

  def withBorderRadius(radius: BorderRadius): ContainerTheme =
    withBorder(
      border match
        case Some(b) => b.withRadius(radius)
        case None    => Border.default.withRadius(radius)
    )
  def square: ContainerTheme       = withBorderRadius(BorderRadius.None)
  def rounded: ContainerTheme      = withBorderRadius(BorderRadius.Medium)
  def roundedSmall: ContainerTheme = withBorderRadius(BorderRadius.Small)
  def roundedLarge: ContainerTheme = withBorderRadius(BorderRadius.Large)
  def circular: ContainerTheme     = withBorderRadius(BorderRadius.Full)

  def withBoxShadow(boxShadow: BoxShadow): ContainerTheme =
    this.copy(boxShadow = Some(boxShadow))

  def noBoxShadow: ContainerTheme =
    this.copy(boxShadow = None)

  def modifyBoxShadow(f: BoxShadow => BoxShadow): ContainerTheme =
    withBoxShadow(
      boxShadow match
        case Some(s) => f(s)
        case None    => f(BoxShadow.none)
    )

  def shadowSmall(color: RGBA): ContainerTheme =
    withBoxShadow(BoxShadow.small(color))
  def shadowMedium(color: RGBA): ContainerTheme =
    withBoxShadow(BoxShadow.medium(color))
  def shadowLarge(color: RGBA): ContainerTheme =
    withBoxShadow(BoxShadow.large(color))
  def shadowExtraLarge(color: RGBA): ContainerTheme =
    withBoxShadow(BoxShadow.extraLarge(color))

  def toStyle: Style =
    val borderStyle = border.map(_.toStyle).getOrElse(Style.empty)
    val shadowStyle = boxShadow.map(_.toStyle).getOrElse(Style.empty)
    borderStyle |+| shadowStyle

object ContainerTheme:

  val default: ContainerTheme =
    ContainerTheme(
      border = None,
      boxShadow = None
    )
