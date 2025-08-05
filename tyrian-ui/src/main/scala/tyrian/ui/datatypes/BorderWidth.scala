package tyrian.ui.datatypes

/** Border width options using rem units. */
enum BorderWidth derives CanEqual:
  case None, Thin, Medium, Thick
  case Custom(value: String)

  def toCSSValue: String = this match
    case None          => "0"
    case Thin          => "0.0625rem" // 1px at 16px base
    case Medium        => "0.125rem"  // 2px at 16px base
    case Thick         => "0.25rem"   // 4px at 16px base
    case Custom(value) => value

object BorderWidth:
  val default: BorderWidth = None
