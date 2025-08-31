package tyrian.ui.datatypes

enum Extent derives CanEqual:
  case Auto, Fill, FitContent
  case Percent(value: Int)
  case Relative(value: Double)
  case CSS(value: String)

  def toCSSValue: String = this match
    case Auto            => "auto"
    case Fill            => "100%"
    case FitContent      => "fit-content"
    case Percent(value)  => s"${value}%"
    case Relative(value) => s"${value}rem"
    case CSS(value)      => value

object Extent:

  def rem(value: Double): Extent =
    Extent.Relative(value)

  def px(value: Int): Extent =
    Extent.CSS(s"${value}px")

  def percent(value: Int): Extent =
    Extent.Percent(value)

  val xSmall: Extent = Extent.Relative(4)  // 64px at 16px base
  val small: Extent  = Extent.Relative(8)  // 128px at 16px base
  val medium: Extent = Extent.Relative(16) // 256px at 16px base
  val large: Extent  = Extent.Relative(24) // 384px at 16px base
  val xLarge: Extent = Extent.Relative(32) // 512px at 16px base
