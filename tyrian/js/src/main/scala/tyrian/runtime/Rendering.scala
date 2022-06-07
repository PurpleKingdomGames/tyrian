package tyrian.runtime

import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom._
import snabbdom.modules._
import tyrian.Attribute
import tyrian.Event
import tyrian.Html
import tyrian.NamedAttribute
import tyrian.Property
import tyrian.Tag
import tyrian.Text

object Rendering:

  def toVNode[Msg](html: Html[Msg], onMsg: Msg => Unit): VNode =
    html match
      case Tag(name, attrs, children) =>
        val as: List[(String, String)] =
          attrs.collect {
            case Attribute(n, v)   => (n, v)
            case NamedAttribute(n) => (n, "")
          }

        val props: List[(String, PropValue)] =
          attrs.collect { case Property(n, v) => (n, v) }

        val events: List[(String, EventHandler)] =
          attrs.collect { case Event(n, msg) =>
            (n, EventHandler((e: dom.Event) => onMsg(msg.asInstanceOf[dom.Event => Msg](e))))
          }

        val data: VNodeData =
          VNodeData.empty.copy(
            props = props.toMap,
            attrs = as.toMap,
            on = events.toMap
          )

        val childrenElem: Array[VNode] =
          children.toArray.map {
            case t: Text            => VNode.text(t.value)
            case subHtml: Html[Msg] => toVNode(subHtml, onMsg)
          }

        h(name, data, childrenElem)

  private lazy val patch: Patch =
    snabbdom.init(
      Seq(
        Attributes.module,
        Classes.module,
        Props.module,
        Styles.module,
        EventListeners.module,
        Dataset.module
      )
    )

  def render[Model, Msg](oldNode: Element | VNode, model: Model, view: Model => Html[Msg], onMsg: Msg => Unit): VNode =
    oldNode match
      case em: Element => patch(em, Rendering.toVNode(view(model), onMsg))
      case vn: VNode   => patch(vn, Rendering.toVNode(view(model), onMsg))
