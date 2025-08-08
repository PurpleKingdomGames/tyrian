package tyrian.ui.datatypes

enum FontSize derives CanEqual:
  case XSmall, Small, Medium, Large, XLarge, XXLarge
  case Relative(value: Double)
  case CSS(value: String)

  def toCSSValue: String =
    this match
      case XSmall          => "0.75rem"  // 12px at 16px base
      case Small           => "0.875rem" // 14px at 16px base
      case Medium          => "1rem"     // 16px at 16px base
      case Large           => "1.125rem" // 18px at 16px base
      case XLarge          => "1.5rem"   // 24px at 16px base
      case XXLarge         => "2rem"     // 32px at 16px base
      case Relative(value) => s"${value}rem"
      case CSS(value)      => value

object FontSize:
  val default: FontSize = Medium

  val heading1: FontSize = FontSize.Relative(2)     // 32px at 16px base
  val heading2: FontSize = FontSize.Relative(1.75)  // 28px at 16px base
  val heading3: FontSize = FontSize.Relative(1.5)   // 24px at 16px base
  val heading4: FontSize = FontSize.Relative(1.25)  // 20px at 16px base
  val heading5: FontSize = FontSize.Relative(1.125) // 18px at 16px base
  val heading6: FontSize = FontSize.Relative(1)     // 16px at 16px base

  val caption: FontSize = XSmall
  val button: FontSize  = Medium
  val label: FontSize   = Small
