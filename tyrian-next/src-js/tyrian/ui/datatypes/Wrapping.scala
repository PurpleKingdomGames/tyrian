package tyrian.ui.datatypes

enum Wrapping derives CanEqual:
  case Wrap, NoWrap

  /** Used with the `white-space` CSS property */
  def toTextCSSValue: String =
    this match
      case Wrapping.Wrap   => "normal"
      case Wrapping.NoWrap => "nowrap"

  /** Used with the `flex-wrap` CSS property */
  def toFlexCSSValue: String =
    this match
      case Wrapping.Wrap   => "wrap"
      case Wrapping.NoWrap => "nowrap"
