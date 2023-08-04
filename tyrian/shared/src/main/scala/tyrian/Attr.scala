package tyrian

import scala.util.Try

/** HTML attribute */
sealed trait Attr[+M]:
  def map[N](f: M => N): Attr[N]

/** An attribute of an HTML tag that does not exist, used as a "do not render" placeholder
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
final case class PropertyString(name: String, value: String) extends Property:
  type Out = String
  def map[N](f: Nothing => N): PropertyString = this
  val valueOf: Out                            = value

/** Property of a DOM node instance
  *
  * Properties are a type of attribute that can only be set, not removed.
  */
final case class PropertyBoolean(name: String, value: Boolean) extends Property:
  type Out = Boolean
  def map[N](f: Nothing => N): PropertyBoolean = this
  val valueOf: Out                             = value

sealed trait Property extends Attr[Nothing]:
  type Out
  def name: String
  val valueOf: Out

object Property:
  val empty: Property = PropertyString("", "")

  def apply(name: String, value: String | Boolean): Property =
    value match
      case x: String  => PropertyString(name, x)
      case x: Boolean => PropertyBoolean(name, x)

  def fromString(str: String): Option[Property] =
    def asBoolean(s: String): Option[Boolean | String] =
      Try(s.toBoolean).toOption

    str.split("=").toList match
      case name :: value :: Nil =>
        val v: Boolean | String =
          asBoolean(value).getOrElse(value)

        Some(Property(name, v))

      case name :: value :: tail =>
        Some(Property(name, value + tail.mkString))

      case _ =>
        None

/** Event handler
  *
  * @param name
  *   Event name (e.g. `"click"`)
  * @param msg
  *   Message to produce when the event is triggered
  */
final case class Event[E <: Tyrian.Event, M](
    name: String,
    msg: E => M,
    preventDefault: Boolean,
    stopPropagation: Boolean,
    stopImmediatePropagation: Boolean
) extends Attr[M]:
  def map[N](f: M => N): Attr[N] = Event(name, msg andThen f)

  def withPreventDefault(enabled: Boolean): Event[E, M] = this.copy(preventDefault = enabled)
  def usePreventDefault: Event[E, M]                    = withPreventDefault(true)
  def noPreventDefault: Event[E, M]                     = withPreventDefault(false)

  def withStopPropagation(enabled: Boolean): Event[E, M] = this.copy(stopPropagation = enabled)
  def useStopPropagation: Event[E, M]                    = withStopPropagation(true)
  def noStopPropagation: Event[E, M]                     = withStopPropagation(false)

  def withStopImmediatePropagation(enabled: Boolean): Event[E, M] = this.copy(stopImmediatePropagation = enabled)
  def useStopImmediatePropagation: Event[E, M]                    = withStopImmediatePropagation(true)
  def noStopImmediatePropagation: Event[E, M]                     = withStopImmediatePropagation(false)

object Event:
  def apply[E <: Tyrian.Event, M](name: String, msg: E => M): Event[E, M] =
    Event(
      name = name,
      msg = msg,
      preventDefault = true,
      stopPropagation = true,
      stopImmediatePropagation = true
    )
