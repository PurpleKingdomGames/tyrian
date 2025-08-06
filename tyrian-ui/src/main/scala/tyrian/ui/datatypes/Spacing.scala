package tyrian.ui.datatypes

enum Spacing derives CanEqual:
  case None, XSmall, Small, Medium, Large, XLarge, XXLarge
  case Relative(value: Double)
  case CSS(value: String)

  def toCSSValue: String =
    this match
      case None            => "0"
      case XSmall          => "0.25rem" // 4px at 16px base
      case Small           => "0.5rem"  // 8px at 16px base
      case Medium          => "1rem"    // 16px at 16px base
      case Large           => "1.5rem"  // 24px at 16px base
      case XLarge          => "2rem"    // 32px at 16px base
      case XXLarge         => "3rem"    // 48px at 16px base
      case Relative(value) => s"${value}rem"
      case CSS(value)      => value

object Spacing:

  val default: Spacing = Medium

  val tight: Spacing       = Small
  val comfortable: Spacing = Large

  def px(value: Int): Spacing =
    Spacing.CSS(s"${value}px")

  def rem(value: Double): Spacing =
    Spacing.Relative(value)
