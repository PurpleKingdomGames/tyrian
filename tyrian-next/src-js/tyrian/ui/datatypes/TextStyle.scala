package tyrian.ui.datatypes

enum TextStyle derives CanEqual:
  case Normal, Italic

  def toCSSValue: String =
    this match
      case Normal => "normal"
      case Italic => "italic"

object TextStyle:

  val default: TextStyle = Normal
