package tyrian.ui.datatypes

/** Border style options. */
enum BorderStyle derives CanEqual:
  case None, Solid, Dashed, Dotted, Double, Groove, Ridge, Inset, Outset

  def toCSSValue: String = this match
    case None   => "none"
    case Solid  => "solid"
    case Dashed => "dashed"
    case Dotted => "dotted"
    case Double => "double"
    case Groove => "groove"
    case Ridge  => "ridge"
    case Inset  => "inset"
    case Outset => "outset"

object BorderStyle:

  val default: BorderStyle =
    BorderStyle.None
