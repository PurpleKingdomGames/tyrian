package tyrian.ui.datatypes

opaque type Opacity = Double

object Opacity:

  val Full: Opacity    = 1.0
  val High: Opacity    = 0.8
  val Medium: Opacity  = 0.5
  val Low: Opacity     = 0.3
  val VeryLow: Opacity = 0.1
  val None: Opacity    = 0.0

  val default: Opacity = Full

  def apply(value: Double): Opacity =
    Math.min(1.0, Math.max(0.0, value))

  def percent(value: Int): Opacity =
    Math.min(1.0, Math.max(0.0, value / 100.0))

  extension (opacity: Opacity)
    def value: Double      = opacity
    def toCSSValue: String = opacity.toString
