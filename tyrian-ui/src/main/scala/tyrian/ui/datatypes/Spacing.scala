package tyrian.ui.datatypes

/** Spacing options for padding and margins using rem units for accessibility. */
enum Spacing derives CanEqual:
  case None, XSmall, Small, Medium, Large, XLarge, XXLarge
  case Custom(value: String)

  /** Converts the spacing to its CSS value using rem units. */
  def toCSSValue: String = this match
    case None          => "0"
    case XSmall        => "0.25rem" // 4px at 16px base
    case Small         => "0.5rem"  // 8px at 16px base
    case Medium        => "1rem"    // 16px at 16px base
    case Large         => "1.5rem"  // 24px at 16px base
    case XLarge        => "2rem"    // 32px at 16px base
    case XXLarge       => "3rem"    // 48px at 16px base
    case Custom(value) => value

object Spacing:
  /** Default spacing for most UI elements. */
  val default: Spacing = Medium

  /** Common spacing presets for consistent design. */
  val tight: Spacing       = Small
  val comfortable: Spacing = Large
