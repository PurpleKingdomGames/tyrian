package tyrian.ui.datatypes

enum Align derives CanEqual:
  case Top, Middle, Bottom

  def toCSSValue: String =
    this match
      case Top    => "flex-start"
      case Middle => "center"
      case Bottom => "flex-end"

object Align:

  val default: Align = Align.Top
