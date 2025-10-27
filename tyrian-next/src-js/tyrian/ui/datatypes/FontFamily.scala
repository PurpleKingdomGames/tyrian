package tyrian.ui.datatypes

enum FontFamily derives CanEqual:
  case Stack(stack: FontStack)
  case CSS(value: String)

  def toCSSValue: String =
    this match
      case Stack(fonts) => fonts.toCSSValue
      case CSS(value)   => value

object FontFamily:

  def fromCSS(css: String): FontFamily =
    FontFamily.CSS(css)

  def apply(stack: FontStack): FontFamily =
    FontFamily.Stack(stack)

  def apply(primary: FontName, fallbacks: FontName*): FontFamily =
    FontFamily.Stack(FontStack(primary, fallbacks.toList))

  // Pre-computed sans-serif font family stack
  val sansSerif: FontFamily =
    FontFamily.CSS(
      FontFamily.Stack(FontStack.sansSerif).toCSSValue
    )

  // Pre-computed serif font family stack
  val serif: FontFamily =
    FontFamily.CSS(
      FontFamily.Stack(FontStack.serif).toCSSValue
    )

  // Pre-computed monospace font family stack
  val monospace: FontFamily =
    FontFamily.CSS(
      FontFamily.Stack(FontStack.monospace).toCSSValue
    )
