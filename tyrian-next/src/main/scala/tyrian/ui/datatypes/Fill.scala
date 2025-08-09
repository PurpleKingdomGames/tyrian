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

  final case class LinearGradient(
      angle: Radians,
      fromColor: RGBA,
      toColor: RGBA
  ) extends Fill derives CanEqual:

    def withAngle(a: Radians): LinearGradient =
      this.copy(angle = a)

    def withFromColor(c: RGBA): LinearGradient =
      this.copy(fromColor = c)

    def withToColor(c: RGBA): LinearGradient =
      this.copy(toColor = c)

  object LinearGradient:
    val default: LinearGradient =
      LinearGradient(Radians(0), RGBA.Black, RGBA.White)

  final case class RadialGradient(
      center: Position,
      radius: Int,
      fromColor: RGBA,
      toColor: RGBA
  ) extends Fill derives CanEqual:

    def withCenter(p: Position): RadialGradient =
      this.copy(center = p)

    def withRadius(r: Int): RadialGradient =
      this.copy(radius = r)

    def withFromColor(c: RGBA): RadialGradient =
      this.copy(fromColor = c)

    def withToColor(c: RGBA): RadialGradient =
      this.copy(toColor = c)

  object RadialGradient:
    val default: RadialGradient =
      RadialGradient(Position.Center, 100, RGBA.Black, RGBA.White)

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

        case LinearGradient(angle, from, to) =>
          Style("background-image", s"linear-gradient(${angle.toDegrees}deg, ${from.toCSSValue}, ${to.toCSSValue})")

        case RadialGradient(center, _, from, to) =>
          Style(
            "background-image",
            s"radial-gradient(circle at ${center.toCSSValue}, ${from.toCSSValue}, ${to.toCSSValue})"
          )

        case Image(url, position, mode) =>
          Style(
            "background-image"    -> s"url('${url}')",
            "background-position" -> position.toCSSValue
          ) |+| mode.toStyle
