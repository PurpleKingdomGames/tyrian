package tyrian.ui.elements.stateless.button

enum ButtonKind:
  case Button, Reset, Submit

  def toAttributeValue: String =
    this match
      case Button => "button"
      case Reset  => "reset"
      case Submit => "submit"
