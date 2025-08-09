package tyrian.ui.datatypes

enum BackgroundRepeat derives CanEqual:
  case NoRepeat, Repeat, RepeatX, RepeatY, Space, Round
  case CSS(value: String)

  def toCSSValue: String = this match
    case NoRepeat   => "no-repeat"
    case Repeat     => "repeat"
    case RepeatX    => "repeat-x"
    case RepeatY    => "repeat-y"
    case Space      => "space"
    case Round      => "round"
    case CSS(value) => value
