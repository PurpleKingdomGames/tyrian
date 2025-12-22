package tyrian.ui.datatypes

enum BorderCollapse derives CanEqual:
  case Separate, Collapse

  def toCSSValue: String =
    this match
      case BorderCollapse.Separate => "separate"
      case BorderCollapse.Collapse => "collapse"
