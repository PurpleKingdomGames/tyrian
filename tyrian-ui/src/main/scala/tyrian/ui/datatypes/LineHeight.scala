package tyrian.ui.datatypes

enum LineHeight derives CanEqual:
  case Tight, Normal, Relaxed, Loose
  case Relative(value: Double)
  case CSS(value: String)

  def toCSSValue: String =
    this match
      case Tight         => "1.2rem"
      case Normal        => "1.4rem"
      case Relaxed       => "1.5rem"
      case Loose         => "1.7rem"
      case Relative(value) => s"${value}rem"
      case CSS(value) => value

object LineHeight:
  val default: LineHeight = Normal

  val heading: LineHeight = Tight
  val body: LineHeight    = Relaxed
  val caption: LineHeight = Normal
