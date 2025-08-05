package tyrian.ui.image

import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius

// TODO: Box / drop shadow maybe? What else?
final case class ImageTheme(
    border: Option[Border]
):

  def withBorder(border: Border): ImageTheme =
    this.copy(border = Some(border))

  def noBorder: ImageTheme =
    this.copy(border = None)

  def withBorderRadius(radius: BorderRadius): ImageTheme =
    this.copy(border = Some(Border.default.withRadius(radius)))

  def toStyle: Style =
    border.map(_.toStyle).getOrElse(Style.empty)

object ImageTheme:

  val default: ImageTheme =
    ImageTheme(
      border = None
    )
