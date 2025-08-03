package tyrian.ui.datatypes

enum LineHeight derives CanEqual:
  case Tight, Normal, Relaxed, Loose
  case Custom(value: String)

  def toCSSValue: String = this match
    case Tight         => "1.2"
    case Normal        => "1.4"
    case Relaxed       => "1.5"
    case Loose         => "1.7"
    case Custom(value) => value

object LineHeight:
  val default: LineHeight = Normal

  val heading: LineHeight = Tight
  val body: LineHeight    = Relaxed
  val caption: LineHeight = Normal
