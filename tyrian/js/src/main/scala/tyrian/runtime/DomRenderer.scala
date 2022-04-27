package tyrian.runtime

import org.scalajs.dom
import org.scalajs.dom.NodeList
import tyrian.*
import util.Functions

object DomRenderer:

  def render[Model, Msg](
      container: dom.Element,
      oldNode: dom.Element, // unused for now
      model: Model,
      view: Model => Html[Msg],
      callback: Msg => Unit
  ): dom.Element =
    val elem = renderHtml(view(model), callback)
    container.replaceChildren(elem) // This messes everything up - this is where the vdom comes in.
    elem

  def renderHtml[Msg](html: Html[Msg], callback: Msg => Unit): dom.Element =
    html match
      case tag: Tag[_] =>
        val elem = dom.document.createElement(tag.name)

        tag.attributes.foreach { a =>
          addAttribute(a, elem, callback)
        }

        tag.children.foreach { c =>
          val e = renderElem(c, callback)
          elem.appendChild(e)
        }

        elem

  def renderElem[Msg](elem: Elem[Msg], callback: Msg => Unit): dom.Element | dom.Text =
    elem match
      case t: Text      => dom.document.createTextNode(t.value)
      case h: Html[Msg] => renderHtml(h, callback)

  def addAttribute[Msg](a: Attr[Msg], elem: dom.Element, callback: Msg => Unit): Unit =
    a match
      case evt: Event[_, _] =>
        elem.addEventListener(
          evt.name,
          Functions.fun((e: dom.Event) => callback(evt.msg.asInstanceOf[dom.Event => Msg](e)))
        )

      case a: Attribute =>
        elem.setAttribute(a.name, a.value)

      case p: Property =>
        elem.setAttribute(p.name, p.value)

      case a: NamedAttribute =>
        elem.setAttribute(a.name, "")

      case _: EmptyAttribute.type =>
        ()
