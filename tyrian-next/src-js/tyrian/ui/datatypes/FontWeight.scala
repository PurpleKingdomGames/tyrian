package tyrian.ui.datatypes

enum FontWeight derives CanEqual:
  case Thin, Light, Normal, Medium, SemiBold, Bold, ExtraBold, Black

  def toCSSValue: String =
    this match
      case Thin      => "100"
      case Light     => "300"
      case Normal    => "400"
      case Medium    => "500"
      case SemiBold  => "600"
      case Bold      => "700"
      case ExtraBold => "800"
      case Black     => "900"

object FontWeight:

  val default: FontWeight = Normal
