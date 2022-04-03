package tyrian.runtime

import cats.effect.IO
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

type RunWithCallback[Msg] = IO[Option[Msg]] => (Either[Throwable, Option[Msg]] => Unit) => Unit

final class TyrianRuntime[Model, Msg](
    init: (Model, Cmd[Msg]),
    update: (Msg, Model) => (Model, Cmd[Msg]),
    view: Model => Html[Msg],
    subscriptions: Model => Sub[Msg],
    node: Element,
    runner: RunWithCallback[Msg]
) extends SnabbdomSyntax:

  private val (initState, initCmd) = init
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentState: Model = initState
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var vnode: VNode = render(node, currentState, runner)

  // The currently live subs.
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var currentSubscriptions: List[(String, IO[Unit])] = Nil
  // This is a queue of new subs waiting to be run for the first time.
  // In the event that two events happen at once, you can't assume that
  // you would have run all the subs between events.
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var aboutToRunSubscriptions: Set[String] = Set.empty

  def onMsg(run: RunWithCallback[Msg])(msg: Msg): Unit =
    val (updatedState: Model, cmd: Cmd[Msg]) = update(msg, currentState)
    currentState = updatedState
    vnode = render(vnode, currentState, run)
    performSideEffects(cmd, subscriptions(currentState), onMsg(run), run)

  given CanEqual[Option[Msg], Option[Msg]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def performSideEffects(cmd: Cmd[Msg], sub: Sub[Msg], callback: Msg => Unit, run: RunWithCallback[Msg]): Unit =
    // Cmds
    val cmdsToRun = CmdRunner.cmdToTaskList(cmd)

    // Subs
    val allSubs                 = SubRunner.flatten(sub)
    val (stillAlive, discarded) = SubRunner.aliveAndDead(allSubs, currentSubscriptions)
    val newSubs                 = SubRunner.findNewSubs(allSubs, stillAlive.map(_._1), aboutToRunSubscriptions.toList)

    // Update the first run queue
    aboutToRunSubscriptions = aboutToRunSubscriptions ++ newSubs.map(_.id)
    // Update the current subs
    currentSubscriptions = stillAlive

    val subsToRun =
      SubRunner
        .toRun(newSubs, callback)
        .map(_.map { sub =>
          // Remove from the queue
          aboutToRunSubscriptions = aboutToRunSubscriptions - sub.id
          // Add to the current subs
          currentSubscriptions = (sub.id -> sub.cancel) :: currentSubscriptions

          Option.empty[Msg]
        })

    val subsToDiscard =
      discarded.map(_.map(_ => Option.empty[Msg]))

    // Run them all
    (cmdsToRun ++ subsToRun ++ subsToDiscard).foreach { task =>
      run(task) {
        case Right(Some(msg)) => callback(msg)
        case Right(None)      => ()
        case Left(e)          => throw e
      }
    }

  def toVNode(html: Html[Msg], run: RunWithCallback[Msg]): VNode =
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
            (n, fun((e: dom.Event) => onMsg(run)(msg.asInstanceOf[dom.Event => Msg](e))))
          }: _*)

        val childrenElem: List[VNodeParam] =
          children.map {
            case t: Text            => VNodeParam.Text(t.value)
            case subHtml: Html[Msg] => VNodeParam.Node(toVNode(subHtml, run))
          }

        h(name, obj(props = props, attrs = as, on = events))(childrenElem: _*)

  private lazy val patch: (VNode | dom.Element, VNode) => VNode =
    snabbdom.snabbdom.init(
      js.Array(snabbdom.modules.props, snabbdom.modules.attributes, snabbdom.modules.eventlisteners)
    )

  def render(oldNode: Element | VNode, model: Model, run: RunWithCallback[Msg]): VNode =
    patch(oldNode, toVNode(view(model), run))

  def start(): Unit =
    performSideEffects(initCmd, subscriptions(currentState), onMsg(runner), runner)
