package tyrian.runtime

import org.scalajs.dom
import org.scalajs.dom.Element
import snabbdom.SnabbdomSyntax
import snabbdom.VNode
import snabbdom.VNodeParam
import tyrian.Attr
import tyrian.Attribute
import tyrian.Cmd
import tyrian.Event
import tyrian.Hook
import tyrian.Html
import tyrian.Property
import tyrian.Sub
import tyrian.Tag
import tyrian.Task
import tyrian.Task.Cancelable
import tyrian.Task.Observer
import tyrian.Text
import util.Functions.fun

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => obj}

final class TyrianRuntime[Model, Msg](
    init: (Model, Cmd[Msg]),
    update: (Msg, Model) => (Model, Cmd[Msg]),
    view: Model => Html[Msg],
    subscriptions: Model => Sub[Msg],
    node: Element
) extends SnabbdomSyntax:

  private val (initState, initCmd) = init
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentState: Model = initState
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentSubscriptions: List[(String, Cancelable)] = Nil
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var aboutToRunSubscriptions: Set[String] = Set.empty
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var vnode = render(node, currentState)

  def async(thunk: => Unit): Unit =
    js.timers.setTimeout(0)(thunk)

  def onMsg(msg: Msg): Unit = {
    val (updatedState: Model, cmd: Cmd[Msg]) = update(msg, currentState)
    currentState = updatedState
    vnode = render(vnode, currentState)
    performSideEffects(cmd, subscriptions(currentState), onMsg)
  }

  def performSideEffects(cmd: Cmd[Msg], sub: Sub[Msg], callback: Msg => Unit): Unit = {
    CmdRunner.runCmd(cmd, callback, async)

    val allSubs = {
      def loop(sub: Sub[Msg]): List[Sub.OfObservable[_, _, Msg]] =
        sub match
          case Sub.Empty               => Nil
          case Sub.Combine(sub1, sub2) => loop(sub1) ++ loop(sub2)
          case s: Sub.OfObservable[_, _, _] =>
            List(s.asInstanceOf[Sub.OfObservable[_, _, Msg]])

      loop(sub)
    }

    val (stillActives, discarded) =
      currentSubscriptions.partition { case (id, _) => allSubs.exists(_.id == id) }

    val newSubs =
      allSubs.filter(s => stillActives.forall(_._1 != s.id) && !aboutToRunSubscriptions.contains(s.id))

    aboutToRunSubscriptions = aboutToRunSubscriptions ++ newSubs.map(_.id)
    currentSubscriptions = stillActives

    async {
      discarded.foreach(_._2.cancel())

      newSubs.foreach { case Sub.OfObservable(id, observable, f) =>
        val cancelable = observable.run(TaskRunner.asObserver(f andThen callback))
        aboutToRunSubscriptions = aboutToRunSubscriptions - id
        currentSubscriptions = (id -> cancelable) :: currentSubscriptions
      }
    }

  }

  def toVNode(html: Html[Msg]): VNode =
    html match {
      case Tag(name, attrs, children) =>
        val as = js.Dictionary(attrs.collect { case Attribute(n, v) => (n, v) }: _*)

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

      case Hook(model, renderer) =>
        renderer.render(model)
    }

  private lazy val patch =
    snabbdom.snabbdom.init(
      js.Array(snabbdom.modules.props, snabbdom.modules.attributes, snabbdom.modules.eventlisteners)
    )

  def render(oldNode: Element | VNode, model: Model): VNode =
    patch(oldNode, toVNode(view(model)))

  def start(): Unit =
    performSideEffects(initCmd, subscriptions(currentState), onMsg)

end TyrianRuntime
