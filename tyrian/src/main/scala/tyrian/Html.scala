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

object Html extends HtmlTags with HtmlAttributes:

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

  // Custom attribute syntax

  def dataAttr(name: String, value: String): Attr[Nothing]        = Attribute("data-" + name, value)

  def onKeyDown[M](msg: KeyboardEvent => M): Attr[M] = onEvent("keydown", msg)
  def onKeyUp[M](msg: KeyboardEvent => M): Attr[M]   = onEvent("keyup", msg)
  def onInput[M](msg: String => M): Attr[M] =
    onEvent("input", (e: dom.Event) => msg(e.target.asInstanceOf[HTMLInputElement].value))

  def style(name: String, value: String): Attr[Nothing] = Attribute("style", Style(name, value).toString)
  @targetName("style_Style")
  def style(style: Style): Attr[Nothing]    = Attribute("style", style.toString)
  def styles(styles: Style*): Attr[Nothing] = Attribute("style", Monoid.combineAll(styles).toString)
  @targetName("style_tuples")
  def styles(styles: (String, String)*): Attr[Nothing] =
    Attribute("style", Monoid.combineAll(styles.map(p => Style(p._1, p._2))).toString)

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
