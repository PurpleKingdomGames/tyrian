package scalm

import org.scalajs.dom
import org.scalajs.dom.Element
import scalm.Task.{Cancelable, Observer}
import snabbdom.{SnabbdomSyntax, VNode, VNodeParam}
import snabbdom.VNodeParam._
import util.Functions.fun

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => obj}
import scala.scalajs.js.|

/**
  * Scalm runtime implementation
  */
trait Scalm extends SnabbdomSyntax {

  /** Application to run */
  val app: App
  /** DOM element to mount the application to */
  def node: Element

  // Initialize the app
  private val (initState, initCmd) = app.init
  private var currentState = initState
  private var currentSubscriptions: List[(String, Cancelable)] = Nil
  private var aboutToRunSubscriptions: Set[String] = Set.empty
  private var vnode = render(node, currentState)

  performSideEffects(initCmd, app.subscriptions(currentState), onMsg)

  // TODO Check that there is no space leak
  private def onMsg(msg: app.Msg): Unit = {
    val (updatedState, cmd) = app.update(msg, currentState)
    currentState = updatedState
    vnode = render(vnode, currentState)
    performSideEffects(cmd, app.subscriptions(currentState), onMsg)
  }

  private def performSideEffects[Msg, Model](cmd: Cmd[Msg], sub: Sub[Msg], callback: Msg => Unit): Unit = {
    // TODO Optimize by batching the cmd and sub
    cmd match {
      case Cmd.Empty => ()
      case Cmd.RunTask(task, f) => async(execTask(task, f andThen callback))
    }

    val allSubs = {
      def loop(sub: Sub[Msg]): List[Sub.OfObservable[_, _, Msg]] =
        sub match {
          case Sub.Empty                      => Nil
          case Sub.Combine(sub1, sub2)        => loop(sub1) ++ loop(sub2)
          case s: Sub.OfObservable[_, _, Msg] => List(s)
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
      case Task.Succeeded(value) => _notify(Right(value))
      case Task.Failed(error) => _notify(Left(error))
      case Task.RunObservable(observable) => val _ = observable.run(asObserver(_notify)) // FIXME cancellation
      case t @ Task.Mapped(_, _) => execTaskMapped(t, _notify)
      case t @ Task.Recovered(_, _) => execTaskRecovered(t, _notify)
      case t @ Task.Multiplied(_, _) => execTaskMultiplied(t, _notify)
      case t @ Task.FlatMapped(_, _) => execTaskFlatMapped(t, _notify)
    }

  private def asObserver[Err, Success](_notify: Either[Err, Success] => Unit): Observer[Err, Success] =
    new Observer[Err, Success] {
      def onNext(value: Success): Unit = _notify(Right(value))
      def onError(error: Err): Unit = _notify(Left(error))
    }

  private def execTaskMapped[Err, Success, Success2](mapped: Task.Mapped[Err, Success, Success2], notify: Either[Err, Success2] => Unit): Unit =
    execTask[Err, Success](mapped.task, msg => notify(msg.map(mapped.f)))

  private def execTaskRecovered[Err, Success](recovered: Task.Recovered[Err, Success], notify: Either[Err, Success] => Unit): Unit =
    execTask[Err, Success](recovered.task, {
      case Left(err) => execTask[Err, Success](recovered.f(err), notify)
      case Right(success) => notify(Right(success))
    })

  private def execTaskMultiplied[Err, Success1, Success2](multiplied: Task.Multiplied[Err, Success1, Success2], notify: Either[Err, (Success1, Success2)] => Unit): Unit = {
    type Result1 = Either[Err, Success1]
    type Result2 = Either[Err, Success2]
    var r1: Option[Result1] = None
    var r2: Option[Result2] = None
    def notifyProduct(): Unit =
      (r1, r2) match {
        case (Some(Right(s1)), Some(Right(s2))) => notify(Right((s1, s2)))
        case (Some(Left(e)), _) => notify(Left(e))
        case (_, Some(Left(e))) => notify(Left(e))
        case (_, _) => ()
      }
    execTask[Err, Success1](multiplied.task1, result => {
      if (r2.forall(_.isRight)) {
        r1 = Some(result)
        notifyProduct()
      }
    })
    execTask[Err, Success2](multiplied.task2, result => {
      if (r1.forall(_.isRight)) {
        r2 = Some(result)
        notifyProduct()
      }
    })
  }

  private def execTaskFlatMapped[Err, Success, Success2](flatMapped: Task.FlatMapped[Err, Success, Success2], notify: Either[Err, Success2] => Unit): Unit =
    execTask[Err, Success](flatMapped.task, result => {
      val _ = result.foreach(success => execTask(flatMapped.f(success), notify))
    })

  private lazy val patch =
    snabbdom.snabbdom.init(js.Array(snabbdom.modules.props, snabbdom.modules.attributes, snabbdom.modules.eventlisteners))

  /**
    * Patches the DOM to render the current application state
    * @param oldNode DOM node to render to, or previous VDOM node
    * @param model Current state
    * @return The computed VDOM
    */
  def render(oldNode: Element | VNode, model: app.Model): VNode = {
    patch(oldNode, toVNode(app.view(model)))
  }

  private def toVNode(html: Html[app.Msg]): VNode = {
    html match {
      case Tag(name, attrs, children) =>
        val as = js.Dictionary(attrs.collect { case Attribute(n, v) => (n, v) }: _*)
        val props =
          js.Dictionary(attrs.collect { case Prop(n, v) => (n, v) }: _*)
        val events =
          js.Dictionary(attrs.collect { case Event(n, msg) => (n, fun((e: dom.Event) => onMsg(msg.asInstanceOf[dom.Event => app.Msg](e)))) }: _*)
        val childrenElem: Seq[VNodeParam] =
          children.map {
            case Text(s)                 => s
            case subHtml: Html[app.Msg] => toVNode(subHtml)
            case Elem.Empty              => Nil
          }
        h(name, obj(props = props, attrs = as, on = events))(childrenElem: _*)
      case Hook(model, renderer) => renderer.render(model)
    }
  }

}

object Scalm {

  /**
    * Computes the initial state of the given application,
    * renders it on the given DOM element, and listen to
    * user actions
    * @param _app the application to start
    * @param _node the DOM element to mount the app to
    * @return The scalm runtime
    */
  def start(_app: App, _node: Element): Scalm =
    new Scalm {
      lazy val app: _app.type = _app
      def node = _node
    }

  /**
    * Computes the initial state of the given application,
    * renders it on the given DOM element, and listens to
    * user actions
    * @param _node the DOM element to mount the app to
    * @param _init initial state
    * @param _update state transition function
    * @param _view view function
    * @tparam _Model Type of model
    * @tparam _Msg Type of messages
    * @return The scalm runtime
    */
  def start[_Model, _Msg](
    _node: Element
  )(
    _init: _Model, _update: (_Msg, _Model) => _Model, _view: _Model => Html[_Msg]
  ): Scalm = new Scalm {
    lazy val app: App = new App {
      type Msg = _Msg
      type Model = _Model
      def init: (Model, Cmd[Msg]) = pure(_init)
      def view(model: _Model): Html[_Msg] = _view(model)
      def update(msg: _Msg, model: _Model): (_Model, Cmd[_Msg]) = pure(_update(msg, model))
      def subscriptions(model: _Model): Sub[_Msg] = Sub.Empty
    }
    def node: Element = _node
  }

}
