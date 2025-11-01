package tyrian.ui.datatypes

import tyrian.Style

enum LayoutDirection derives CanEqual:
  case Row, Column

  def toCSSValue: String =
    this match
      case Row    => "row"
      case Column => "column"

  def toStyle: Style =
    Style("flex-direction" -> toCSSValue)
