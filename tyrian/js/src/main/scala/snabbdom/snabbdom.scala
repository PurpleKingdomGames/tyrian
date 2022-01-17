package snabbdom

import org.scalajs.dom.Element
import org.scalajs.dom.Text

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

@JSImport("snabbdom", JSImport.Default)
@js.native
object snabbdom extends js.Object:
  @nowarn
  def init(modules: js.Array[js.Object]): js.Function2[VNode | Element, VNode, VNode] = js.native

@SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
@JSImport("snabbdom", "h")
@js.native
object h extends js.Function3[String, js.UndefOr[js.Any], js.UndefOr[js.Any], VNode]:
  def apply(selector: String, b: js.UndefOr[js.Any] = js.undefined, c: js.UndefOr[js.Any] = js.undefined): VNode =
    js.native

@js.native
trait VNode extends js.Object:
  val selector: js.UndefOr[String]
  val data: js.UndefOr[VNodeData]
  val children: js.UndefOr[js.Array[VNode | String]]
  val text: js.UndefOr[String]
  val elm: js.UndefOr[Element | Text]
  val key: js.UndefOr[String | Double]

@js.native
trait VNodeData extends js.Object

// --- Convenient syntax

@SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
trait SnabbdomSyntax extends Any:
  final def e(selector: String, opts: js.UndefOr[js.Object] = js.undefined): VNode =
    _root_.snabbdom.h(selector, opts)

  final def h(selector: String, opts: js.Object)(children: VNodeParam*): VNode =
    _root_.snabbdom.h(
      selector,
      opts,
      js.Array(children.flatMap(_.asVnodes): _*)
    )

  final def h(selector: String)(children: VNodeParam*): VNode =
    h(selector, js.Dynamic.literal())(children: _*)

sealed trait VNodeParam:
  def asVnodes: List[String | VNode]

object VNodeParam:

  final case class Text(s: String) extends VNodeParam:
    def asVnodes: List[String | VNode] = List(s)

  final case class Node(vnode: VNode) extends VNodeParam:
    def asVnodes: List[String | VNode] = List(vnode)
