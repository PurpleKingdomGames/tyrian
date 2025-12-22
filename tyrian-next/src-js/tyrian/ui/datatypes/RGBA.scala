package tyrian.ui.datatypes

final case class RGBA(r: Double, g: Double, b: Double, a: Double) derives CanEqual:
  def +(other: RGBA): RGBA =
    RGBA.combine(this, other)

  def withRed(newRed: Double): RGBA =
    this.copy(r = newRed)

  def withGreen(newGreen: Double): RGBA =
    this.copy(g = newGreen)

  def withBlue(newBlue: Double): RGBA =
    this.copy(b = newBlue)

  def withAlpha(newAlpha: Double): RGBA =
    this.copy(a = newAlpha)

  def withAmount(amount: Double): RGBA =
    withAlpha(amount)

  def makeOpaque: RGBA =
    this.copy(a = 1d)

  def makeTransparent: RGBA =
    this.copy(a = 0d)

  def mix(other: RGBA, amount: Double): RGBA = {
    val mix = Math.min(1.0, Math.max(0.0, amount))
    RGBA(
      (r * (1.0 - mix)) + (other.r * mix),
      (g * (1.0 - mix)) + (other.g * mix),
      (b * (1.0 - mix)) + (other.b * mix),
      (a * (1.0 - mix)) + (other.a * mix)
    )
  }
  def mix(other: RGBA): RGBA =
    mix(other, 0.5)

  def toRGB: RGB =
    RGB(r, g, b)

  def toHex: String =
    val convert: Double => String = d =>
      val hex = Integer.toHexString((Math.min(1, Math.max(0, d)) * 255).toInt)
      if hex.length == 1 then "0" + hex else hex

    convert(r) + convert(g) + convert(b) + convert(a)

  def toHexString(prefix: String): String =
    prefix + toHex

  def toCSSValue: String =
    s"rgba(${255 * r}, ${255 * g}, ${255 * b}, ${255 * a})"

  def toArray: Array[Float] =
    Array(r.toFloat, g.toFloat, b.toFloat, a.toFloat)

object RGBA:

  val Red: RGBA     = RGBA(1, 0, 0, 1)
  val Green: RGBA   = RGBA(0, 1, 0, 1)
  val Blue: RGBA    = RGBA(0, 0, 1, 1)
  val Yellow: RGBA  = RGBA(1, 1, 0, 1)
  val Magenta: RGBA = RGBA(1, 0, 1, 1)
  val Cyan: RGBA    = RGBA(0, 1, 1, 1)
  val White: RGBA   = RGBA(1, 1, 1, 1)
  val Black: RGBA   = RGBA(0, 0, 0, 1)

  val Normal: RGBA = White
  val None: RGBA   = White
  val Zero: RGBA   = RGBA(0, 0, 0, 0)

  // https://en.wikipedia.org/wiki/X11_color_names
  val Coral: RGBA     = fromHex("#FF7F50")
  val Crimson: RGBA   = fromHex("#DC143C")
  val DarkBlue: RGBA  = fromHex("#00008B")
  val Indigo: RGBA    = fromHex("#4B0082")
  val Olive: RGBA     = fromHex("#808000")
  val Orange: RGBA    = fromHex("#FFA500")
  val Pink: RGBA      = fromHex("#FFC0CB")
  val Plum: RGBA      = fromHex("#DDA0DD")
  val Purple: RGBA    = fromHex("#A020F0")
  val Salmon: RGBA    = fromHex("#FA8072")
  val SeaGreen: RGBA  = fromHex("#2E8B57")
  val Silver: RGBA    = fromHex("#C0C0C0")
  val SlateGray: RGBA = fromHex("#708090")
  val SteelBlue: RGBA = fromHex("#4682B4")
  val Teal: RGBA      = fromHex("#008080")
  val Thistle: RGBA   = fromHex("#D8BFD8")
  val Tomato: RGBA    = fromHex("#FF6347")

  def apply(r: Double, g: Double, b: Double): RGBA =
    RGBA(r, g, b, 1.0)

  def combine(a: RGBA, b: RGBA): RGBA =
    (a, b) match {
      case (RGBA.None, x) =>
        x
      case (x, RGBA.None) =>
        x
      case (x, y) =>
        RGBA(x.r + y.r, x.g + y.g, x.b + y.b, x.a + y.a)
    }

  def fromHex(hex: String): RGBA =
    hex match {
      case h if h.startsWith("0x") && h.length == 10 =>
        fromColorInts(
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16),
          Integer.parseInt(hex.substring(8, 10), 16)
        )

      case h if h.startsWith("0x") && h.length == 8 =>
        fromColorInts(
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16)
        )

      case h if h.startsWith("#") && h.length == 9 =>
        fromColorInts(
          Integer.parseInt(hex.substring(1, 3), 16),
          Integer.parseInt(hex.substring(3, 5), 16),
          Integer.parseInt(hex.substring(5, 7), 16),
          Integer.parseInt(hex.substring(7, 9), 16)
        )

      case h if h.startsWith("#") && h.length == 7 =>
        fromColorInts(
          Integer.parseInt(hex.substring(1, 3), 16),
          Integer.parseInt(hex.substring(3, 5), 16),
          Integer.parseInt(hex.substring(5, 7), 16)
        )

      case h if h.length == 8 =>
        fromColorInts(
          Integer.parseInt(hex.substring(0, 2), 16),
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4, 6), 16),
          Integer.parseInt(hex.substring(6, 8), 16)
        )

      case h if h.length == 6 =>
        fromColorInts(
          Integer.parseInt(hex.substring(0, 2), 16),
          Integer.parseInt(hex.substring(2, 4), 16),
          Integer.parseInt(hex.substring(4), 16)
        )

      case _ =>
        RGBA.Black
    }

  def fromColorInts(r: Int, g: Int, b: Int): RGBA =
    RGBA((1.0 / 255) * r, (1.0 / 255) * g, (1.0 / 255) * b, 1.0)

  def fromColorInts(r: Int, g: Int, b: Int, a: Int): RGBA =
    RGBA((1.0 / 255) * r, (1.0 / 255) * g, (1.0 / 255) * b, (1.0 / 255) * a)
