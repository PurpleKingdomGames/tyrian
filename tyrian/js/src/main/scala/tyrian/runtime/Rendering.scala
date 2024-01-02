package tyrian.runtime

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.window
import snabbdom._
import snabbdom.modules._
import tyrian.Attr
import tyrian.Attribute
import tyrian.Empty
import tyrian.Event
import tyrian.Html
import tyrian.Location
import tyrian.NamedAttribute
import tyrian.PropertyBoolean
import tyrian.PropertyString
import tyrian.RawTag
import tyrian.Tag
import tyrian.Text

import scala.scalajs.js

object Rendering:

  private def buildNodeData[Msg](attrs: List[Attr[Msg]], onMsg: Msg => Unit, key: Option[String]): VNodeData =
    val as: List[(String, String)] =
      attrs.collect {
        case Attribute(n, v)   => (n, v)
        case NamedAttribute(n) => (n, "")
      }

    val props: List[(String, PropValue)] =
      attrs.collect {
        case PropertyString(n, v)  => (n, v)
        case PropertyBoolean(n, v) => (n, v)
      }

    val events: List[(String, EventHandler)] =
      attrs.collect { case Event(n, msg, preventDefault, stopPropagation, stopImmediatePropagation) =>
        val callback: dom.Event => Unit = { (e: dom.Event) =>
          if preventDefault then e.preventDefault()
          if stopPropagation then e.stopPropagation()
          if stopImmediatePropagation then e.stopImmediatePropagation()

          onMsg(msg.asInstanceOf[dom.Event => Msg](e))
        }

        (n, EventHandler(callback))
      }

    VNodeData.empty.copy(
      props = props.toMap,
      attrs = as.toMap,
      on = events.toMap,
      key = key
    )

  private def interceptHref[Msg](attrs: List[Attr[Msg]]): Boolean =
    val href = attrs.exists {
      case Attribute("href", _) => true
      case _                    => false
    }

    val onClick = attrs.exists {
      case Event("click", _, _, _, _) => true
      case _                          => false
    }

    href && !onClick

  private def onClickPreventDefault[Msg](
      attrs: List[Attr[Msg]],
      onMsg: Msg => Unit,
      router: Location => Msg
  ): (String, EventHandler) =
    val newLocation = attrs.collect { case Attribute("href", loc) =>
      loc
    }.headOption

    val callback: dom.Event => Unit = { (e: dom.Event) =>

      e.preventDefault()

      newLocation match
        case None =>
          ()

        case Some(loc) =>
          val jsLoc           = Location.fromJsLocation(window.location)
          val locationToRoute = Location.fromUrl(loc, jsLoc)

          if locationToRoute.isInternal then
            // Updates the address bar
            window.history.pushState(new js.Object, "", loc)

          // Invoke the page change
          onMsg(router(locationToRoute))

          ()

    }

    "click" -> EventHandler(callback)

  def toVNode[Msg](html: Html[Msg], onMsg: Msg => Unit, router: Location => Msg): VNode =
    html match
      case RawTag(name, attrs, html, key) =>
        val data = buildNodeData(attrs, onMsg, key)
        val elm  = dom.document.createElement(name)
        elm.innerHTML = html
        val vNode = snabbdom.toVNode(elm)
        vNode.data = data
        vNode

      // Intercept a tags with an href and no onClick attribute to stop the
      // browser following links by default.
      case Tag("a", attrs, children, key) if interceptHref(attrs) =>
        val data = buildNodeData(attrs, onMsg, key)
        val childrenElem: Array[VNode] =
          children.toArray.map {
            case _: Empty.type      => VNode.empty()
            case t: Text            => VNode.text(t.value)
            case subHtml: Html[Msg] => toVNode(subHtml, onMsg, router)
          }

        h(
          "a",
          data.copy(on = data.on + onClickPreventDefault(attrs, onMsg, router)),
          childrenElem
        )

      case Tag(name, attrs, children, key) =>
        val data = buildNodeData(attrs, onMsg, key)
        val childrenElem: Array[VNode] =
          children.toArray.map {
            case _: Empty.type      => VNode.empty()
            case t: Text            => VNode.text(t.value)
            case subHtml: Html[Msg] => toVNode(subHtml, onMsg, router)
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

  def render[Model, Msg](
      oldNode: Element | VNode,
      model: Model,
      view: Model => Html[Msg],
      onMsg: Msg => Unit,
      router: Location => Msg
  ): VNode =
    oldNode match
      case em: Element => patch(em, Rendering.toVNode(view(model), onMsg, router))
      case vn: VNode   => patch(vn, Rendering.toVNode(view(model), onMsg, router))
