package tyrian

/** HTML attribute */
sealed trait Attr[+M]:
  def map[N](f: M => N): Attr[N]

/** An attribute of an HTML tag that is does not exist, used as a "do not render" placeholder
  */
case object EmptyAttribute extends Attr[Nothing]:
  def map[N](f: Nothing => N): EmptyAttribute.type = this

/** An attribute of an HTML tag that only has a name, no value
  */
final case class NamedAttribute(name: String) extends Attr[Nothing]:
  def map[N](f: Nothing => N): NamedAttribute = this

/** Attribute of an HTML tag
  *
  * Attributes are like properties, but can be removed. This is important for attributes like disabled, hidden, selected
  * where there is no value, they are present and therefore set, or absent and unset.
  */
final case class Attribute(name: String, value: String) extends Attr[Nothing]:
  def map[N](f: Nothing => N): Attribute = this
object Attribute:
  val empty: Attribute = Attribute("", "")
  def fromString(str: String): Option[Attribute] =
    str.split("=").toList match
      case name :: value :: Nil  => Some(Attribute(name, value))
      case name :: value :: tail => Some(Attribute(name, value + tail.mkString))
      case _                     => None

/** Property of a DOM node instance
  *
  * Properties are a type of attribute that can only be set, not removed.
  */
final case class Property(name: String, value: String) extends Attr[Nothing]:
  def map[N](f: Nothing => N): Property = this
object Property:
  val empty: Property = Property("", "")
  def fromString(str: String): Option[Property] =
    str.split("=").toList match
      case name :: value :: Nil  => Some(Property(name, value))
      case name :: value :: tail => Some(Property(name, value + tail.mkString))
      case _                     => None

/** Event handler
  *
  * @param name
  *   Event name (e.g. `"click"`)
  * @param msg
  *   Message to produce when the event is triggered
  */
final case class Event[E <: Tyrian.Event, M](name: String, msg: E => M) extends Attr[M]:
  def map[N](f: M => N): Attr[N] = Event(name, msg andThen f)
