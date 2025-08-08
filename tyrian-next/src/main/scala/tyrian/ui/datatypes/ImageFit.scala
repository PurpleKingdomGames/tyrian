package tyrian.ui.datatypes

import tyrian.Style

/** Tells the image how to fill the box, equivalent to CSS Object-Fit. */
enum ImageFit derives CanEqual:
  case Fill, Contain, Cover, ScaleDown, None

  def toCSSValue: String =
    this match
      case Fill      => "fill"
      case Contain   => "contain"
      case Cover     => "cover"
      case ScaleDown => "scale-down"
      case None      => "none"

  def toStyle: Style =
    Style("object-fit" -> toCSSValue)

object ImageFit:

  val default: ImageFit =
    ImageFit.Fill
