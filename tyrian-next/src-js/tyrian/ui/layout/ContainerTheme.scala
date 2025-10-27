package tyrian.ui.layout

import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.Fill
import tyrian.ui.datatypes.Opacity
import tyrian.ui.datatypes.RGB
import tyrian.ui.datatypes.RGBA

final case class ContainerTheme(
    border: Option[Border],
    boxShadow: Option[BoxShadow],
    opacity: Option[Opacity],
    backgroundFill: Option[Fill]
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
    modifyBorder(_.withRadius(radius))

  def withBorderColor(value: RGBA): ContainerTheme =
    modifyBorder(_.withColor(value))

  def withBorderWidth(value: BorderWidth): ContainerTheme =
    modifyBorder(_.withWidth(value))

  def withBorderStyle(value: BorderStyle): ContainerTheme =
    modifyBorder(_.withStyle(value))

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

  def withOpacity(opacity: Opacity): ContainerTheme =
    this.copy(opacity = Some(opacity))

  def noOpacity: ContainerTheme =
    this.copy(opacity = None)

  def fullyOpaque: ContainerTheme     = withOpacity(Opacity.Full)
  def semiTransparent: ContainerTheme = withOpacity(Opacity.Medium)
  def transparent: ContainerTheme     = withOpacity(Opacity.None)

  def withBackgroundColor(color: RGB): ContainerTheme =
    this.copy(backgroundFill = Some(Fill.Color(color)))
  def withBackgroundColor(color: RGBA): ContainerTheme =
    this.copy(backgroundFill = Some(Fill.Color(color)))

  def withBackgroundFill(fill: Fill): ContainerTheme =
    this.copy(backgroundFill = Some(fill))

  def noBackground: ContainerTheme =
    this.copy(backgroundFill = None)

  def toStyle: Style =
    val borderStyle     = border.map(_.toStyle).getOrElse(Style.empty)
    val shadowStyle     = boxShadow.map(_.toStyle).getOrElse(Style.empty)
    val opacityStyle    = opacity.map(o => Style("opacity", o.toCSSValue)).getOrElse(Style.empty)
    val backgroundStyle = backgroundFill.map(_.toStyle).getOrElse(Style.empty)

    borderStyle |+| shadowStyle |+| opacityStyle |+| backgroundStyle

object ContainerTheme:

  val default: ContainerTheme =
    ContainerTheme(
      border = None,
      boxShadow = None,
      opacity = None,
      backgroundFill = None
    )
