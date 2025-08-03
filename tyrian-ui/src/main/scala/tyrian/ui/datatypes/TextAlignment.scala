package tyrian.ui.datatypes

enum TextAlignment derives CanEqual:
  case Left, Center, Right, Justify

  def toCSSValue: String = this match
    case Left    => "left"
    case Center  => "center"
    case Right   => "right"
    case Justify => "justify"

object TextAlignment:
  
  val default: TextAlignment = Left
