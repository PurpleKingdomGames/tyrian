package tyrian.ui.datatypes

import scala.annotation.targetName
import scala.math

opaque type Degrees = Double

object Degrees:

  inline def apply(degrees: Double): Degrees =
    degrees

  val zero: Degrees  = Degrees(0)
  val `45`: Degrees  = Degrees(45)
  val `90`: Degrees  = Degrees(90)
  val `180`: Degrees = Degrees(180)
  val `270`: Degrees = Degrees(270)
  val `360`: Degrees = Degrees(360)

  inline def fromRadians(radians: Radians): Degrees =
    radians.toDegrees

  def mod(dividend: Degrees, divisor: Degrees): Degrees =
    Degrees((dividend % divisor + divisor) % divisor)

  extension (d: Degrees)
    def +(other: Degrees): Degrees =
      Degrees(d + other)
    @targetName("+_Double")
    def +(other: Double): Degrees =
      Degrees(d + other)

    def -(other: Degrees): Degrees =
      Degrees(d - other)
    @targetName("-_Double")
    def -(other: Double): Degrees =
      Degrees(d - other)

    def *(other: Degrees): Degrees =
      Degrees(d * other)
    @targetName("*_Double")
    def *(other: Double): Degrees =
      Degrees(d * other)

    def /(other: Degrees): Degrees =
      Degrees(d / other)
    @targetName("/_Double")
    def /(other: Double): Degrees =
      Degrees(d / other)

    def %(other: Degrees): Degrees =
      Degrees.mod(d, other)
    @targetName("%_Double")
    def %(other: Double): Degrees =
      Degrees.mod(d, other)

    def wrap: Degrees =
      val m = d % Degrees.`360`
      Degrees(if m < 0 then m + Degrees.`360` else m)

    def centeredWrap: Degrees =
      val w = (d + Degrees.`180`) % Degrees.`360`
      Degrees(if w < 0 then w + Degrees.`360` else w) - Degrees.`180`

    def negative: Degrees =
      -d

    def invert: Degrees =
      negative

    def `unary_-`: Degrees =
      negative

    def ~==(other: Degrees): Boolean =
      Math.abs(d.toDouble - other.toDouble) < 0.001

    def max(other: Degrees): Degrees =
      math.max(d, other)

    def min(other: Degrees): Degrees =
      math.min(d, other)

    inline def toDouble: Double =
      d

    def toFloat: Float =
      d.toFloat

    def toRadians: Radians =
      Radians.fromDegrees(d)

    def toCSSValue: String =
      s"${d.toString}deg"
