package tyrian.ui.layout

import tyrian.ui.UIElement
import tyrian.ui.datatypes.LayoutDirection

object Column:

  def apply(children: UIElement[?]*): Layout =
    Layout(LayoutDirection.Column, children.toList)
