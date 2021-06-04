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

/** A text node */
final case class Text(value: String) extends Elem[Nothing]:
  def map[N](f: Nothing => N): Text = this

/** Base class for HTML tags */
sealed trait Html[+M] extends Elem[M]:
  def map[N](f: M => N): Html[N]

object Html:

  // Tag syntax

  def tag[M](name: String)(attributes: Attr[M]*)(children: Elem[M]*): Html[M] = Tag(name, attributes, children)

  def button[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] = tag("button")(attributes: _*)(children: _*)
  def div[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]    = tag("div")(attributes: _*)(children: _*)
  def span[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]   = tag("span")(attributes: _*)(children: _*)
  def h1[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("h1")(attributes: _*)(children: _*)
  def h2[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("h2")(attributes: _*)(children: _*)
  def h3[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("h3")(attributes: _*)(children: _*)
  def h4[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("h4")(attributes: _*)(children: _*)
  def h5[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("h5")(attributes: _*)(children: _*)
  def h6[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("h6")(attributes: _*)(children: _*)
  def input[M](attributes: Attr[M]*): Html[M]                      = tag("input")(attributes: _*)()
  def radio[M](name: String, checked: Boolean, attributes: Attr[M]*): Html[M] =
    input(
      Seq(
        Property("type", "radio"),
        Property("name", name),
        if checked then Property("checked", "checked") else Property.empty
      ) ++ attributes: _*
    )
  def label[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] = tag("label")(attributes: _*)(children: _*)
  def ol[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]    = tag("ol")(attributes: _*)(children: _*)
  def ul[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]    = tag("ul")(attributes: _*)(children: _*)
  def li[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]    = tag("li")(attributes: _*)(children: _*)
  def img[M](attributes: Attr[M]*): Html[M]                       = tag("img")(attributes: _*)()
  def a[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]     = tag("a")(attributes: _*)(children: _*)
  def br: Html[Nothing]                                           = tag("br")()()
  def hr: Html[Nothing]                                           = tag("hr")()()
  def title[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] = tag("title")(attributes: _*)(children: _*)
  def style[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] = tag("style")(attributes: _*)(children: _*)
  def p[M](children: Elem[M]*): Html[M]                           = tag("p")()(children: _*)
  def i[M](children: Elem[M]*): Html[M]                           = tag("i")()(children: _*)
  def b[M](children: Elem[M]*): Html[M]                           = tag("b")()(children: _*)
  def em[M](children: Elem[M]*): Html[M]                          = tag("em")()(children: _*)
  def cite[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]  = tag("cite")(attributes: _*)(children: _*)
  def head[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]  = tag("head")(attributes: _*)(children: _*)
  def body[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M]  = tag("body")(attributes: _*)(children: _*)

  def text(str: String): Text = Text(str)

  // Attribute syntax

  def attribute(name: String, value: String): Attr[Nothing] = Attribute(name, value)
  def attributes(as: (String, String)*): Seq[Attr[Nothing]] = as.map(p => Attribute(p._1, p._2))
  def property(name: String, value: String): Attr[Nothing]  = Property(name, value)
  def properties(ps: (String, String)*): Seq[Attr[Nothing]] = ps.map(p => Property(p._1, p._2))

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
  def styles(styles: Style*): Attr[Nothing]             = Attribute("style", Monoid.combineAll(styles).toString)
  @targetName("style_tuples")
  def styles(styles: (String, String)*): Attr[Nothing] =
    Attribute("style", Monoid.combineAll(styles.map(p => Style(p._1, p._2))).toString)

  def alt(value: String): Attr[Nothing]        = Attribute("alt", value)
  def charset(value: String): Attr[Nothing]    = Attribute("charset", value)
  def checked: Attr[Nothing]                   = Attribute("checked", "checked")
  def `class`(name: String): Attr[Nothing]     = Attribute("class", name)
  def disabled: Attr[Nothing]                  = Attribute("disabled", "disabled")
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
  def value(value: String): Attr[Nothing]      = Attribute("value", value)
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
final case class Tag[+M](name: String, attributes: Seq[Attr[M]], children: Seq[Elem[M]]) extends Html[M]:
  def map[N](f: M => N): Tag[N] =
    Tag(name, attributes.map(_.map(f)), children.map(_.map(f)))
