package tyrian.ui.datatypes

// TODO: Consider just calling this Alignment, top / middle / bottom, left / center / right
//       and then translating to flex later.
enum FlexAlignment derives CanEqual:
  case Start, Center, End, SpaceBetween, SpaceAround, SpaceEvenly, Stretch

  def toCSSValue: String =
    this match
      case Start        => "flex-start"
      case Center       => "center"
      case End          => "flex-end"
      case SpaceBetween => "space-between"
      case SpaceAround  => "space-around"
      case SpaceEvenly  => "space-evenly"
      case Stretch      => "stretch"

object FlexAlignment:

  val default: FlexAlignment = Start
