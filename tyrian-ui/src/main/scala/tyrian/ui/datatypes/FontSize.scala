package tyrian.ui.datatypes

enum FontSize derives CanEqual:
  case XSmall, Small, Medium, Large, XLarge, XXLarge
  case Custom(value: String)

  def toCSSValue: String = this match
    case XSmall        => "0.75rem"  // 12px at 16px base
    case Small         => "0.875rem" // 14px at 16px base
    case Medium        => "1rem"     // 16px at 16px base
    case Large         => "1.125rem" // 18px at 16px base
    case XLarge        => "1.5rem"   // 24px at 16px base
    case XXLarge       => "2rem"     // 32px at 16px base
    case Custom(value) => value

object FontSize:
  val default: FontSize = Medium

  val heading1: FontSize = Custom("2rem")     // 32px at 16px base
  val heading2: FontSize = Custom("1.75rem")  // 28px at 16px base
  val heading3: FontSize = Custom("1.5rem")   // 24px at 16px base
  val heading4: FontSize = Custom("1.25rem")  // 20px at 16px base
  val heading5: FontSize = Custom("1.125rem") // 18px at 16px base
  val heading6: FontSize = Custom("1rem")     // 16px at 16px base

  val caption: FontSize = XSmall
  val button: FontSize  = Medium
  val label: FontSize   = Small
