package tyrian.ui.datatypes

enum Justify derives CanEqual:
  case Left, Center, Right

  def toCSSValue: String =
    this match
      case Left   => "flex-start"
      case Center => "center"
      case Right  => "flex-end"

object Justify:

  val default: Justify = Justify.Left
