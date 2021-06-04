package scalm

import cats.kernel.Monoid

opaque type Style = String
object Style:

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
    combineAll(styles.map(fromTuple))

  val empty: Style = Style("", "")

  def combine(a: Style, b: Style)(using m: Monoid[Style]): Style =
    m.combine(a, b)

  def combineAll(styles: Seq[Style])(using m: Monoid[Style]): Style =
    styles.foldLeft(Style.empty)(m.combine)

  extension (style: Style) def toString: String = style

  implicit val monoid: Monoid[Style] =
    new Monoid[Style]:
      def empty: Style = Style.empty
      def combine(x: Style, y: Style): Style =
        x.toString + y.toString
