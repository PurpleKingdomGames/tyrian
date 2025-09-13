package tyrian.ui.datatypes

import tyrian.Style

sealed trait Fill
object Fill:

  def None: Color =
    Color(RGBA.Zero)

  final case class Color(color: RGBA) extends Fill derives CanEqual:
    def withColor(newColor: RGBA): Color =
      this.copy(color = newColor)

  object Color:
    val default: Color =
      Color(RGBA.Zero)

    def apply(color: RGB): Color =
      Color(color.toRGBA)

  final case class LinearGradient(
      angle: Degrees,
      color: RGBA,
      colors: List[RGBA]
  ) extends Fill derives CanEqual:

    def withAngle(degrees: Degrees): LinearGradient =
      this.copy(angle = degrees)

    def withColors(color: RGBA, colors: RGBA*): LinearGradient =
      this.copy(color = color, colors = colors.toList)

    def addColors(additional: RGBA*): LinearGradient =
      this.copy(colors = colors ++ additional.toList)

  object LinearGradient:
    val default: LinearGradient =
      LinearGradient(Degrees.zero, RGBA.Black, List(RGBA.White))

    def apply(angle: Degrees): LinearGradient =
      LinearGradient(angle, RGBA.Black, List(RGBA.White))

    def apply(color: RGBA, colors: RGBA*): LinearGradient =
      LinearGradient(Degrees.zero, color, colors.toList)

    def apply(angle: Degrees, color: RGBA, colors: RGBA*): LinearGradient =
      LinearGradient(angle, color, colors.toList)

  final case class RadialGradient(
      center: Position,
      color: RGBA,
      colors: List[RGBA]
  ) extends Fill derives CanEqual:

    def withCenter(p: Position): RadialGradient =
      this.copy(center = p)

    def withColors(color: RGBA, colors: RGBA*): RadialGradient =
      this.copy(color = color, colors = colors.toList)

    def addColors(additional: RGBA*): RadialGradient =
      this.copy(colors = colors ++ additional.toList)

  object RadialGradient:
    val default: RadialGradient =
      RadialGradient(Position.Center, RGBA.Black, List(RGBA.White))

    def apply(center: Position): RadialGradient =
      RadialGradient(center, RGBA.Black, List(RGBA.White))

    def apply(color: RGBA, colors: RGBA*): RadialGradient =
      RadialGradient(Position.Center, color, colors.toList)

    def apply(center: Position, color: RGBA, colors: RGBA*): RadialGradient =
      RadialGradient(center, color, colors.toList)

  final case class Image(url: String, position: Position, mode: BackgroundMode) extends Fill derives CanEqual:
    def withUrl(u: String): Image =
      this.copy(url = u)

    def withPosition(p: Position): Image =
      this.copy(position = p)

    def withMode(m: BackgroundMode): Image =
      this.copy(mode = m)

  object Image:
    def apply(url: String): Image = Image(url, Position.Center, BackgroundMode.default)

  extension (fill: Fill)
    def toStyle: Style =
      fill match
        case Color(c) =>
          Style("background-color", c.toCSSValue)

        case LinearGradient(angle, color, colors) =>
          val colorStops =
            (color :: colors).map(_.toCSSValue).mkString(", ")

          Style("background-image", s"linear-gradient(${angle.toCSSValue}, ${colorStops})")

        case RadialGradient(position, color, colors) =>
          val colorStops =
            (color :: colors).map(_.toCSSValue).mkString(", ")

          Style(
            "background-image",
            s"radial-gradient(circle at ${position.toCSSValue}, ${colorStops})"
          )

        case Image(url, position, mode) =>
          Style(
            "background-image"    -> s"url('${url}')",
            "background-position" -> position.toCSSValue
          ) |+| mode.toStyle
