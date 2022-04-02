package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.effect.syntax.*
import cats.syntax.all.*
import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom.SnabbdomSyntax
import snabbdom.VNode
import snabbdom.VNodeParam
import tyrian.Attr
import tyrian.Attribute
import tyrian.Cmd
import tyrian.Event
import tyrian.Html
import tyrian.NamedAttribute
import tyrian.Property
import tyrian.Sub
import tyrian.Tag
import tyrian.Text
import util.Functions.fun

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => obj}

final class TyrianRuntime[F[_]: Async, Model, Msg](
    init: (Model, Cmd[F, Msg]),
    update: (Msg, Model) => (Model, Cmd[F, Msg]),
    view: Model => Html[Msg],
    subscriptions: Model => Sub[F, Msg],
    node: Element
) extends SnabbdomSyntax:

  private val (initState, initCmd) = init
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentState: Model = initState
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var vnode = render(node, currentState)

  private val subRunner: SubRunner[F] = new SubRunner

  def async(thunk: => Unit): Unit =
    js.timers.setTimeout(0)(thunk)

  def onMsg(msg: Msg): Unit = {
    println(">>> onMsg: " + msg.toString)
    val (updatedState: Model, cmd: Cmd[F, Msg]) = update(msg, currentState)
    currentState = updatedState
    vnode = render(vnode, currentState)
    // This F[Unit] is never run.
    performSideEffects(cmd, subscriptions(currentState), onMsg)
  }

  def performSideEffects(cmd: Cmd[F, Msg], sub: Sub[F, Msg], callback: Msg => Unit): F[Unit] =
    for {
    _ <- CmdRunner.runCmd(cmd, callback, async)
    _ <- subRunner.runSub(sub, callback, async)
    } yield ()

  def toVNode(html: Html[Msg]): VNode =
    html match
      case Tag(name, attrs, children) =>
        val as = js.Dictionary(
          attrs.collect {
            case Attribute(n, v)   => (n, v)
            case NamedAttribute(n) => (n, "")
          }: _*
        )

        val props =
          js.Dictionary(attrs.collect { case Property(n, v) => (n, v) }: _*)

        val events =
          js.Dictionary(attrs.collect { case Event(n, msg) =>
            (n, fun((e: dom.Event) => onMsg(msg.asInstanceOf[dom.Event => Msg](e))))
          }: _*)

        val childrenElem: List[VNodeParam] =
          children.map {
            case t: Text            => VNodeParam.Text(t.value)
            case subHtml: Html[Msg] => VNodeParam.Node(toVNode(subHtml))
          }

        h(name, obj(props = props, attrs = as, on = events))(childrenElem: _*)

  private lazy val patch: (VNode | dom.Element, VNode) => VNode =
    snabbdom.snabbdom.init(
      js.Array(snabbdom.modules.props, snabbdom.modules.attributes, snabbdom.modules.eventlisteners)
    )

  def render(oldNode: Element | VNode, model: Model): VNode =
    patch(oldNode, toVNode(view(model)))

  def start(): F[Unit] =
    performSideEffects(initCmd, subscriptions(currentState), onMsg)
