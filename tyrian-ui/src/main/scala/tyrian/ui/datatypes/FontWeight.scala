package tyrian.ui.datatypes

/** Font weight options for text, from thinnest to boldest. */
enum FontWeight derives CanEqual:
  case Thin, Light, Normal, Medium, SemiBold, Bold, ExtraBold, Black

  /** Converts the font weight to its CSS numeric value. */
  def toCSSValue: String = this match
    case Thin      => "100"
    case Light     => "300"
    case Normal    => "400"
    case Medium    => "500"
    case SemiBold  => "600"
    case Bold      => "700"
    case ExtraBold => "800"
    case Black     => "900"

object FontWeight:
  /** Default font weight for body text. */
  val default: FontWeight = Normal
