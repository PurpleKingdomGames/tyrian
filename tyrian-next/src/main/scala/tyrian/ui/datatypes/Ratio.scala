package tyrian.ui.datatypes

import tyrian.Style

opaque type Ratio = Double

object Ratio:

  def apply(value: Double): Ratio = value
  val one: Ratio                  = 1
  val default: Ratio              = one

  extension (r: Ratio)
    def toDouble: Double   = r
    def toCSSValue: String = s"${r}"
    def toStyle: Style     = Style("flex" -> toCSSValue)
