package tyrian.ui.elements.stateless.text

/** Semantic text variants that determine both styling and HTML element output */
enum TextVariant derives CanEqual:
  case Normal
  case Paragraph
  case Heading1
  case Heading2
  case Heading3
  case Heading4
  case Heading5
  case Heading6
  case Caption
  case Code
  case Label
