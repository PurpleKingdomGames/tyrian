package tyrian.ui.datatypes

enum BackgroundSize derives CanEqual:
  case Auto, Cover, Contain, Fill
  case CSS(value: String)

  def toCSSValue: String = this match
    case Auto       => "auto"
    case Cover      => "cover"
    case Contain    => "contain"
    case Fill       => "100% 100%"
    case CSS(value) => value
