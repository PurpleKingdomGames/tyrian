package tyrian

import cats.Monoid

opaque type Style = String
object Style:

  given Monoid[Style] =
    new Monoid[Style] {
      def empty: Style =
        Style.empty

      def combine(a: Style, b: Style): Style =
        Style.combine(a, b)
    }

  def fromTuple(t: (String, String)): Style =
    t match
      case ("", "")                   => ""
      case ("", s) if s.endsWith(";") => s
      case ("", s)                    => s + ";"
      case (s, "") if s.endsWith(";") => s
      case (s, "")                    => s + ";"
      case (name, value)              => s"$name:$value;"

  def apply(name: String, value: String): Style =
    fromTuple((name, value))

  def apply(styles: (String, String)*): Style =
    combineAll(styles.toList.map(fromTuple))

  val empty: Style = Style("", "")

  def combine(a: Style, b: Style): Style =
    a.toString + b.toString

  def combineAll(styles: List[Style]): Style =
    styles.foldLeft(Style.empty)(combine)

  extension (style: Style)
    def toString: String = style
    def |+|(other: Style) =
      combine(style, other)
