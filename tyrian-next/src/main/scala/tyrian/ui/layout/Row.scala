package tyrian.ui.layout

import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection

object Row:

  def apply(children: UIElement[?]*): Layout =
    Layout(LayoutDirection.Row, children.toList)
