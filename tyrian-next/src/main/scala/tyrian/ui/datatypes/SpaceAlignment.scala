package tyrian.ui.datatypes

enum SpaceAlignment derives CanEqual:
  case SpaceBetween, SpaceAround, SpaceEvenly, Stretch

  def toCSSValue: String =
    this match
      case SpaceBetween => "space-between"
      case SpaceAround  => "space-around"
      case SpaceEvenly  => "space-evenly"
      case Stretch      => "stretch"

object SpaceAlignment:

  val default: SpaceAlignment = SpaceAlignment.Stretch
