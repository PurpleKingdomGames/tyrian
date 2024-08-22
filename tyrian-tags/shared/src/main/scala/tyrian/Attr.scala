package tyrian

import scala.util.Try

/** HTML attribute */
sealed trait Attr[+M]:
  def map[N](f: M => N): Attr[N]
  override def toString(): String = this.render

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
  def map[N](f: M => N): Event[E, N] = this.copy(msg = msg andThen f)

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

trait AttributeSyntax:

  final def attr(name: String): AttributeName = AttributeName(name)
  final def prop(name: String): PropertyName  = PropertyName(name)

  def attribute(name: String, value: String): Attr[Nothing]                 = AttributeSyntax.attribute(name, value)
  def attributes(as: (String, String)*): List[Attr[Nothing]]                = AttributeSyntax.attributes(as.toList)
  def property(name: String, value: Boolean | String): Attr[Nothing]        = AttributeSyntax.property(name, value)
  def properties(ps: (String, Boolean | String)*): List[Attr[Nothing]]      = AttributeSyntax.properties(ps.toList)
  def onEvent[E <: Tyrian.Event, M](name: String, msg: E => M): Event[E, M] = AttributeSyntax.onEvent(name, msg)

object AttributeSyntax:

  def attribute(name: String, value: String): Attr[Nothing]                 = Attribute(name, value)
  def attributes(as: List[(String, String)]): List[Attr[Nothing]]           = as.map(p => Attribute(p._1, p._2))
  def property(name: String, value: Boolean | String): Attr[Nothing]        = Property(name, value)
  def properties(ps: List[(String, Boolean | String)]): List[Attr[Nothing]] = ps.map(p => Property(p._1, p._2))

  def onEvent[E <: Tyrian.Event, M](name: String, msg: E => M): Event[E, M] = Event(name, msg)

final class AttributeName(name: String):
  def :=(value: String | Int | Double | Boolean): Attribute =
    value match
      case x: String  => Attribute(name.toString, x)
      case x: Int     => Attribute(name.toString, x.toString)
      case x: Double  => Attribute(name.toString, x.toString)
      case x: Boolean => Attribute(name.toString, x.toString)

final class PropertyName(name: String):
  def :=(value: String | Boolean): Attribute =
    value match
      case x: String  => Attribute(name.toString, x)
      case x: Boolean => Attribute(name.toString, x.toString)

final class AttributeNameString(name: String):
  def :=(value: String): Attribute = Attribute(name.toString, value)

final class AttributeNameInt(name: String):
  def :=(value: Int): Attribute = Attribute(name.toString, value.toString)

final class AttributeNameDouble(name: String):
  def :=(value: Double): Attribute = Attribute(name.toString, value.toString)

final class AttributeNameBoolean(name: String):
  def :=(value: Boolean): Attribute = Attribute(name.toString, value.toString)

final class AttributeNameStyle(name: String):
  def :=(value: Style): Attribute = Attribute(name.toString, value.toString)

final class PropertyNameString(name: String):
  def :=(value: String): PropertyString = PropertyString(name.toString, value)

final class PropertyNameBoolean(name: String):
  def :=(value: Boolean): PropertyBoolean = PropertyBoolean(name.toString, value)
