package tyrian.ui.datatypes

enum BorderRadius derives CanEqual:
  case None, Small, Medium, Large, Full
  case Percent(value: Int)
  case Relative(value: Double)
  case CSS(value: String)

  def toCSSValue: String = this match
    case None            => "0"
    case Small           => "0.125rem" // 2px at 16px base
    case Medium          => "0.25rem"  // 4px at 16px base
    case Large           => "0.5rem"   // 8px at 16px base
    case Full            => "50%"      // Perfect circle/pill shape
    case p: Percent      => s"${p.clamped}px"
    case Relative(value) => s"${value}rem"
    case CSS(value)      => value

object BorderRadius:

  val default: BorderRadius =
    BorderRadius.None

  def percent(value: Int): BorderRadius =
    BorderRadius.Percent(value)

  def px(value: Int): BorderRadius =
    BorderRadius.CSS(s"${value}px")

  def rem(value: Double): BorderRadius =
    BorderRadius.Relative(value)

  object Percent:
    extension (p: Percent)
      def clamped: Percent =
        val c = if p.value < 0 then 0 else if p.value > 100 then 100 else p.value
        Percent(c)
