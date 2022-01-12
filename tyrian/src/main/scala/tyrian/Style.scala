package tyrian

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
