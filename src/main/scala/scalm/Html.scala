package scalm

import cats.kernel.Monoid
import org.scalajs.dom
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.HTMLInputElement
import snabbdom.VNode
import scala.annotation.targetName

/** An HTML element can be a tag or a text node */
sealed trait Elem[+M]:
  def map[N](f: M => N): Elem[N]

/** Base class for HTML tags */
sealed trait Html[+M] extends Elem[M]:
  def map[N](f: M => N): Html[N]

/** An HTML tag */
final case class Tag[+M](name: String, attributes: Seq[Attr[M]], children: Seq[Elem[M]]) extends Html[M]:
  def map[N](f: M => N): Tag[N] =
    Tag(name, attributes.map(_.map(f)), children.map(_.map(f)))

/** Unmanaged HTML tag
  *
  * @param model
  *   current state to render
  * @param renderer
  *   function that renders the given model
  */
final case class Hook[Model](model: Model, renderer: HookRenderer[Model]) extends Html[Nothing]:
  def map[N](f: Nothing => N): Hook[Model] = this

trait HookRenderer[Model]:
  def render(model: Model): VNode

/** A text node */
final case class Text(value: String) extends Elem[Nothing]:
  def map[N](f: Nothing => N): Text = this

object Elem:
  // FIXME Remove?
  case object Empty extends Elem[Nothing]:
    def map[N](f: Nothing => N): this.type = this

/** HTML attribute */
sealed trait Attr[+M]:
  def map[N](f: M => N): Attr[N]

/** Property of a DOM node instance */
final case class Prop(name: String, value: String) extends Attr[Nothing]:
  def map[N](f: Nothing => N): Prop = this

/** Attribute of an HTML tag */
final case class Attribute(name: String, value: String) extends Attr[Nothing]:
  def map[N](f: Nothing => N): Attribute = this

/** Event handler
  *
  * @param name
  *   Event name (e.g. `"click"`)
  * @param msg
  *   Message to produce when the event is triggered
  */
final case class Event[E <: dom.Event, M](name: String, msg: E => M) extends Attr[M]:
  def map[N](f: M => N): Attr[N] = Event(name, msg andThen f)

object Attr:
  case object Empty extends Attr[Nothing]:
    def map[N](f: Nothing => N): this.type = this

object Html:
  def tag[M](name: String)(attributes: Attr[M]*)(children: Elem[M]*): Html[M] = Tag(name, attributes, children)
  def button[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]            = tag("button")(attributes: _*)(children: _*)
  def div[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]               = tag("div")(attributes: _*)(children: _*)
  def span[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]              = tag("span")(attributes: _*)(children: _*)
  def h1[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]                = tag("h1")(attributes: _*)(children: _*)
  def h2[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]                = tag("h2")(attributes: _*)(children: _*)
  def h3[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]                = tag("h3")(attributes: _*)(children: _*)
  def input[M](attributes: Attr[M]*): Html[M]                                 = tag("input")(attributes: _*)()
  def radio[M](name: String, checked: Boolean, attributes: Attr[M]*): Html[M] =
    input(
      Prop("type", "radio") +:
        Prop("name", name) +:
        cond(checked)(Prop("checked", "checked")) +:
        attributes: _*
    )
  def label[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] = tag("label")(attributes: _*)(children: _*)
  def ul[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]    = tag("ul")(attributes: _*)(children: _*)
  def li[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]    = tag("li")(attributes: _*)(children: _*)

  def text(str: String): Text = Text(str)

  def attrs(as: (String, String)*): Seq[Attr[Nothing]] = as.map(p => Attribute(p._1, p._2))
  def attributes(as: (String, String)*): Seq[Attr[Nothing]] = as.map(p => Attribute(p._1, p._2))
  def attr(name: String, value: String): Attr[Nothing]      = Attribute(name, value)

  def onClick[M](msg: M): Attr[M]                    = onEvent("click", (_: dom.Event) => msg)
  def onMouseEnter[M](msg: M): Attr[M]               = onEvent("mouseenter", (_: dom.Event) => msg)
  def onMouseLeave[M](msg: M): Attr[M]               = onEvent("mouseleave", (_: dom.Event) => msg)
  def onMouseDown[M](msg: M): Attr[M]                = onEvent("mousedown", (_: dom.Event) => msg)
  def onMouseUp[M](msg: M): Attr[M]                  = onEvent("mouseup", (_: dom.Event) => msg)
  def onKeyDown[M](msg: KeyboardEvent => M): Attr[M] = onEvent("keydown", msg)
  def onKeyUp[M](msg: KeyboardEvent => M): Attr[M]   = onEvent("keyup", msg)
  def onInput[M](msg: String => M): Attr[M] =
    onEvent("input", (e: dom.Event) => msg(e.target.asInstanceOf[HTMLInputElement].value))
  def onEvent[E <: org.scalajs.dom.Event, M](name: String, msg: E => M): Attr[M] = Event(name, msg)

  def style(s: String): Attr[Nothing]      = Attribute("style", s) // TODO Use CssStyle
  def style(styles: Style*): Attr[Nothing] = Attribute("style", Monoid.combineAll(styles).toString)
  @targetName("style_tuples")
  def style(styles: (String, String)*): Attr[Nothing] =
    Attribute("style", Monoid.combineAll(styles.map(p => Style(p._1, p._2))).toString)

  def id(value: String): Attr[Nothing]     = Attribute("id", value)
  def `class`(name: String): Attr[Nothing] = Attribute("class", name)

  def optional[A, M](maybeA: Option[A])(f: A => Attr[M]): Attr[M] = maybeA.fold[Attr[M]](Attr.Empty)(f)
  def cond[M](b: Boolean)(attr: => Attr[M]): Attr[M]              = if (b) attr else Attr.Empty

  def placeholder(text: String): Attr[Nothing] = attr("placeholder", text)
end Html

opaque type Style = String
object Style:

  def apply(style: String): Style               = style
  def apply(name: String, value: String): Style = s"$name: $value;"

  val empty: Style = Style("")

  extension (style: Style) def toString: String = style

  implicit val monoid: Monoid[Style] =
    new Monoid[Style]:
      def empty: Style                       = Style.empty
      def combine(x: Style, y: Style): Style = Style(x.toString ++ y.toString)
