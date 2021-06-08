package tyrian

import org.scalajs.dom
import org.scalajs.dom.Element
import tyrian.Task.{Cancelable, Observer}
import snabbdom.{SnabbdomSyntax, VNode, VNodeParam}
import util.Functions.fun

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => obj}
import scala.scalajs.js.|

/** Tyrian runtime implementation
  *
  * @param app
  *   Application to run
  * @param node
  *   DOM element to mount the application to
  */
final case class Tyrian[Model, Msg](
    init: (Model, Cmd[Msg]),
    update: (Msg, Model) => (Model, Cmd[Msg]),
    view: Model => Html[Msg],
    subscriptions: Model => Sub[Msg],
    node: Element
) extends SnabbdomSyntax:

  // Initialize the app
  private val (initState, initCmd)                             = init
  private var currentState: Model                              = initState
  private var currentSubscriptions: List[(String, Cancelable)] = Nil
  private var aboutToRunSubscriptions: Set[String]             = Set.empty
  private var vnode                                            = render(node, currentState)

  // TODO Check that there is no space leak
  private def onMsg(msg: Msg): Unit = {
    val (updatedState: Model, cmd: Cmd[Msg]) = update(msg, currentState)
    currentState = updatedState
    vnode = render(vnode, currentState)
    performSideEffects(cmd, subscriptions(currentState), onMsg)
  }

  private def performSideEffects[Msg, Model](cmd: Cmd[Msg], sub: Sub[Msg], callback: Msg => Unit): Unit = {
    // TODO Optimize by batching the cmd and sub
    cmd match {
      case Cmd.Empty            => ()
      case Cmd.RunTask(task, f) => async(execTask(task, f andThen callback))
    }

    val allSubs = {
      def loop(sub: Sub[Msg]): List[Sub.OfObservable[_, _, Msg]] =
        sub match {
          case Sub.Empty               => Nil
          case Sub.Combine(sub1, sub2) => loop(sub1) ++ loop(sub2)
          case s: Sub.OfObservable[_, _, _] =>
            List(s.asInstanceOf[Sub.OfObservable[_, _, Msg]]) // unchecked due to erasure. Hit n' hope?
        }
      loop(sub)
    }

    val (stillActives, discarded) =
      currentSubscriptions.partition { case (id, _) => allSubs.exists(_.id == id) }

    // Subscriptions that were not previously active or being about to run
    val newSubs =
      allSubs.filter(s => stillActives.forall(_._1 != s.id) && !aboutToRunSubscriptions.contains(s.id))

    aboutToRunSubscriptions = aboutToRunSubscriptions ++ newSubs.map(_.id)
    currentSubscriptions = stillActives

    async {
      discarded.foreach(_._2.cancel())
//      val newCancelables =
      newSubs.foreach { case Sub.OfObservable(id, observable, f) =>
        val cancelable = observable.run(asObserver(f andThen callback))
        aboutToRunSubscriptions = aboutToRunSubscriptions - id
        currentSubscriptions = (id -> cancelable) :: currentSubscriptions
//        id -> cancelable
      }
    }

  }

  private def async(thunk: => Unit): Unit = {
    val _ = js.timers.setTimeout(0)(thunk) // FIXME handle cancellation?
  }

  private def execTask[Err, Success](task: Task[Err, Success], _notify: Either[Err, Success] => Unit): Unit =
    task match {
      case Task.Succeeded(value)          => _notify(Right(value))
      case Task.Failed(error)             => _notify(Left(error))
      case Task.RunObservable(observable) => val _ = observable.run(asObserver(_notify)) // FIXME cancellation
      case t @ Task.Mapped(_, _)          => execTaskMapped(t, _notify)
      case t @ Task.Recovered(_, _)       => execTaskRecovered(t, _notify)
      case t @ Task.Multiplied(_, _)      => execTaskMultiplied(t, _notify)
      case t @ Task.FlatMapped(_, _)      => execTaskFlatMapped(t, _notify)
    }

  private def asObserver[Err, Success](_notify: Either[Err, Success] => Unit): Observer[Err, Success] =
    new Observer[Err, Success] {
      def onNext(value: Success): Unit = _notify(Right(value))
      def onError(error: Err): Unit    = _notify(Left(error))
    }

  private def execTaskMapped[Err, Success, Success2](
      mapped: Task.Mapped[Err, Success, Success2],
      notify: Either[Err, Success2] => Unit
  ): Unit =
    execTask[Err, Success](mapped.task, msg => notify(msg.map(mapped.f)))

  private def execTaskRecovered[Err, Success](
      recovered: Task.Recovered[Err, Success],
      notify: Either[Err, Success] => Unit
  ): Unit =
    execTask[Err, Success](
      recovered.task,
      {
        case Left(err)      => execTask[Err, Success](recovered.f(err), notify)
        case Right(success) => notify(Right(success))
      }
    )

  private def execTaskMultiplied[Err, Success1, Success2](
      multiplied: Task.Multiplied[Err, Success1, Success2],
      notify: Either[Err, (Success1, Success2)] => Unit
  ): Unit = {
    type Result1 = Either[Err, Success1]
    type Result2 = Either[Err, Success2]
    var r1: Option[Result1] = None
    var r2: Option[Result2] = None
    def notifyProduct(): Unit =
      (r1, r2) match {
        case (Some(Right(s1)), Some(Right(s2))) => notify(Right((s1, s2)))
        case (Some(Left(e)), _)                 => notify(Left(e))
        case (_, Some(Left(e)))                 => notify(Left(e))
        case (_, _)                             => ()
      }
    execTask[Err, Success1](
      multiplied.task1,
      result =>
        if (r2.forall(_.isRight)) {
          r1 = Some(result)
          notifyProduct()
        }
    )
    execTask[Err, Success2](
      multiplied.task2,
      result =>
        if (r1.forall(_.isRight)) {
          r2 = Some(result)
          notifyProduct()
        }
    )
  }

  private def execTaskFlatMapped[Err, Success, Success2](
      flatMapped: Task.FlatMapped[Err, Success, Success2],
      notify: Either[Err, Success2] => Unit
  ): Unit =
    execTask[Err, Success](
      flatMapped.task,
      result => {
        val _ = result.foreach(success => execTask(flatMapped.f(success), notify))
      }
    )

  private lazy val patch =
    snabbdom.snabbdom.init(
      js.Array(snabbdom.modules.props, snabbdom.modules.attributes, snabbdom.modules.eventlisteners)
    )

  private def toVNode(html: Html[Msg]): VNode =
    html match {
      case Tag(name, attrs, children) =>
        val as = js.Dictionary(attrs.collect { case Attribute(n, v) => (n, v) }: _*)
        val props =
          js.Dictionary(attrs.collect { case Property(n, v) => (n, v) }: _*)
        val events =
          js.Dictionary(attrs.collect { case Event(n, msg) =>
            (n, fun((e: dom.Event) => onMsg(msg.asInstanceOf[dom.Event => Msg](e))))
          }: _*)
        val childrenElem: Seq[VNodeParam] =
          children.map {
            case t: Text            => VNodeParam.liftString(t.value)
            case subHtml: Html[Msg] => toVNode(subHtml)
          }
        h(name, obj(props = props, attrs = as, on = events))(childrenElem: _*)

      case Hook(model, renderer) =>
        renderer.render(model)
    }

  performSideEffects(initCmd, subscriptions(currentState), onMsg)

  /** Patches the DOM to render the current application state
    * @param oldNode
    *   DOM node to render to, or previous VDOM node
    * @param model
    *   Current state
    * @return
    *   The computed VDOM
    */
  def render(oldNode: Element | VNode, model: Model): VNode =
    patch(oldNode, toVNode(view(model)))

end Tyrian

object Tyrian:

  /** Computes the initial state of the given application, renders it on the given DOM element, and listens to user
    * actions
    * @param node
    *   the DOM element to mount the app to
    * @param init
    *   initial state
    * @param update
    *   state transition function
    * @param view
    *   view function
    * @tparam Model
    *   Type of model
    * @tparam Msg
    *   Type of messages
    * @return
    *   The tyrian runtime
    */
  def start[Model, Msg](
      node: Element,
      init: Model,
      update: (Msg, Model) => Model,
      view: Model => Html[Msg]
  ): Tyrian[Model, Msg] =
    Tyrian(
      (init, Cmd.Empty),
      (msg: Msg, m: Model) => (update(msg, m), Cmd.Empty),
      view,
      _ => Sub.Empty,
      node
    )

  /** Computes the initial state of the given application, renders it on the given DOM element, and listens to user
    * actions
    * @param node
    *   the DOM element to mount the app to
    * @param init
    *   initial state
    * @param update
    *   state transition function
    * @param view
    *   view function
    * @param subscriptions
    *   subscriptions function
    * @tparam Model
    *   Type of model
    * @tparam Msg
    *   Type of messages
    * @return
    *   The tyrian runtime
    */
  def start[Model, Msg](
      node: Element,
      init: (Model, Cmd[Msg]),
      update: (Msg, Model) => (Model, Cmd[Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[Msg]
  ): Tyrian[Model, Msg] =
    Tyrian(
      init,
      update,
      view,
      subscriptions,
      node
    )
