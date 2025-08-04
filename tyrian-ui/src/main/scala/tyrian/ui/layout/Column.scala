package tyrian.ui.layout

import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection

object Column:

  def apply[Msg](children: UIElement[Msg]*): Layout[Msg] =
    Layout(LayoutDirection.Column, children.toList)
