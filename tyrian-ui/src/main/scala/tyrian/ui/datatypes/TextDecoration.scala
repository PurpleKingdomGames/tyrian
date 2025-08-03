package tyrian.ui.datatypes

enum TextDecoration derives CanEqual:
  case None, Underline, Strikethrough, Overline

  def toCSSValue: String = this match
    case None          => "none"
    case Underline     => "underline"
    case Strikethrough => "line-through"
    case Overline      => "overline"

object TextDecoration:

  val default: TextDecoration = None
