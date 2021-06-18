package tyrian

import cats.kernel.Monoid
import org.scalajs.dom
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.raw.HTMLInputElement
import snabbdom.VNode
import scala.annotation.targetName

/** An HTML element can be a tag or a text node */
sealed trait Elem[+M]:
  def map[N](f: M => N): Elem[N]

/** A text node */
final case class Text(value: String) extends Elem[Nothing]:
  def map[N](f: Nothing => N): Text = this

/** Base class for HTML tags */
sealed trait Html[+M] extends Elem[M]:
  def map[N](f: M => N): Html[N]

object Html extends HtmlTags:

  // Custom tag syntax

  def radio[M](name: String, checked: Boolean, attributes: Attr[M]*): Html[M] =
    radio(name, checked, attributes.toList)
  @targetName("radio-list")
  def radio[M](name: String, checked: Boolean, attributes: List[Attr[M]]): Html[M] =
    input(
      List(
        Property("type", "radio"),
        Property("name", name),
        if checked then Property("checked", "checked") else Property.empty
      ) ++ attributes: _*
    )

  def text(str: String): Text = Text(str)

  // Attribute syntax

  def attribute(name: String, value: String): Attr[Nothing]  = Attribute(name, value)
  def attributes(as: (String, String)*): List[Attr[Nothing]] = as.toList.map(p => Attribute(p._1, p._2))
  def property(name: String, value: String): Attr[Nothing]   = Property(name, value)
  def properties(ps: (String, String)*): List[Attr[Nothing]] = ps.toList.map(p => Property(p._1, p._2))

  def onChange[M](msg: M): Attr[M]                   = onEvent("change", (_: dom.Event) => msg)
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

  def style(s: String): Attr[Nothing]                   = Attribute("style", s)
  def style(name: String, value: String): Attr[Nothing] = Attribute("style", Style(name, value).toString)
  @targetName("style_Style")
  def style(style: Style): Attr[Nothing]    = Attribute("style", style.toString)
  def styles(styles: Style*): Attr[Nothing] = Attribute("style", Monoid.combineAll(styles).toString)
  @targetName("style_tuples")
  def styles(styles: (String, String)*): Attr[Nothing] =
    Attribute("style", Monoid.combineAll(styles.map(p => Style(p._1, p._2))).toString)

  def accept(value: String): Attr[Nothing]     = Attribute("accept", value)
  def alt(value: String): Attr[Nothing]        = Attribute("alt", value)
  def charset(value: String): Attr[Nothing]    = Attribute("charset", value)
  def checked: Attr[Nothing]                   = Attribute("checked", "checked")
  def `class`(name: String): Attr[Nothing]     = Attribute("class", name)
  def cls(name: String): Attr[Nothing]         = Attribute("class", name)
  def className(name: String): Attr[Nothing]   = Attribute("class", name)
  def _class(name: String): Attr[Nothing]      = Attribute("class", name)
  def disabled: Attr[Nothing]                  = Attribute("disabled", "disabled")
  def height(value: Int): Attr[Nothing]        = Attribute("height", value.toString)
  def height(value: String): Attr[Nothing]     = Attribute("height", value)
  def href(uri: String): Attr[Nothing]         = Attribute("href", uri)
  def id(value: String): Attr[Nothing]         = Attribute("id", value)
  def label(value: String): Attr[Nothing]      = Attribute("label", value)
  def method(value: String): Attr[Nothing]     = Attribute("method", value)
  def name(value: String): Attr[Nothing]       = Attribute("name", value)
  def placeholder(text: String): Attr[Nothing] = Attribute("placeholder", text)
  def src(path: String): Attr[Nothing]         = Attribute("src", path)
  def target(value: String): Attr[Nothing]     = Attribute("target", value)
  def `type`(value: String): Attr[Nothing]     = Attribute("type", value)
  def _type(value: String): Attr[Nothing]      = Attribute("type", value)
  def typ(value: String): Attr[Nothing]        = Attribute("type", value)
  def tpe(value: String): Attr[Nothing]        = Attribute("type", value)
  def value(value: String): Attr[Nothing]      = Attribute("value", value)
  def width(value: Int): Attr[Nothing]         = Attribute("width", value.toString)
  def width(value: String): Attr[Nothing]      = Attribute("width", value)

end Html

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

// -- HTML Tags --

/** An HTML tag */
final case class Tag[+M](name: String, attributes: List[Attr[M]], children: List[Elem[M]]) extends Html[M]:
  def map[N](f: M => N): Tag[N] =
    Tag(name, attributes.map(_.map(f)), children.map(_.map(f)))
