package tyrian.ui.datatypes

enum Position derives CanEqual:
  case Center, Top, Bottom, Left, Right
  case TopLeft, TopCenter, TopRight
  case CenterLeft, CenterRight
  case BottomLeft, BottomCenter, BottomRight
  case Percentage(x: Double, y: Double)
  case Relative(x: Double, y: Double)
  case CSS(value: String)

  def toCSSValue: String = this match
    case Center           => "center"
    case Top              => "top"
    case Bottom           => "bottom"
    case Left             => "left"
    case Right            => "right"
    case TopLeft          => "top left"
    case TopCenter        => "top center"
    case TopRight         => "top right"
    case CenterLeft       => "center left"
    case CenterRight      => "center right"
    case BottomLeft       => "bottom left"
    case BottomCenter     => "bottom center"
    case BottomRight      => "bottom right"
    case Percentage(x, y) => s"${x}% ${y}%"
    case Relative(x, y)   => s"${x}rem ${y}rem"
    case CSS(value)       => value

object Position:
  val default: Position = Center

  def apply(x: Double, y: Double): Position =
    Position.Percentage(x, y)

  def rem(x: Double, y: Double): Position =
    Position.Relative(x, y)

  def percentage(x: Double, y: Double): Position =
    Position.Percentage(x, y)
