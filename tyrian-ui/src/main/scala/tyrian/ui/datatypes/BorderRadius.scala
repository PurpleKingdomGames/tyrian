package tyrian.ui.datatypes

enum BorderRadius derives CanEqual:
  case None, Small, Medium, Large, Full
  case Custom(value: String)

  def toCSSValue: String = this match
    case None          => "0"
    case Small         => "0.125rem" // 2px at 16px base
    case Medium        => "0.25rem"  // 4px at 16px base
    case Large         => "0.5rem"   // 8px at 16px base
    case Full          => "50%"      // Perfect circle/pill shape
    case Custom(value) => value

object BorderRadius:

  val default: BorderRadius =
    BorderRadius.None

  def percent(value: Int): BorderRadius =
    val clamped = if value < 0 then 0 else if value > 100 then 100 else value
    BorderRadius.Custom(s"${clamped}%")

  def px(value: Int): BorderRadius =
    BorderRadius.Custom(s"${value}px")

  def rem(value: Double): BorderRadius =
    BorderRadius.Custom(s"${value}rem")
