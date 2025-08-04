package tyrian.ui.layout

import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection

object Row:

  def apply[Msg](children: UIElement[Msg]*): Layout[Msg] =
    Layout(LayoutDirection.Row, children.toList)
