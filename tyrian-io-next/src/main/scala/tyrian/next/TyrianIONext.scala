package tyrian.next

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalajs.dom.Element
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.*

import scala.scalajs.js.annotation.*

trait TyrianIONext[Model]:

  /** Specifies the number of queued tasks that can be consumed at any one time. Default is 1024 which is assumed to be
    * more than sufficient, however the value can be tweaked in your app by overriding this value.
    */
  def MaxConcurrentTasks: Int = 1024

  val run: IO[Nothing] => Unit = _.unsafeRunAndForget()

  def router: Location => GlobalMsg

  /** Used to initialise your app. Accepts simple flags and produces the initial model state, along with any commands to
    * run at start up, in order to trigger other processes.
    */
  def init(flags: Map[String, String]): Outcome[Model]

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce commands to run.
    */
  def update(model: Model): GlobalMsg => Outcome[Model]

  /** Used to render your current model into an HTML view.
    */
  def view(model: Model): HtmlRoot

  /** Subscriptions are typically processes that run for a period of time and emit discrete events based on some world
    * event, e.g. a mouse moving might emit it's coordinates.
    */
  def watchers(model: Model): Watch

  /** Launch the app and attach it to an element with the given id. Can be called from Scala or JavaScript.
    */
  @JSExport
  def launch(containerId: String): Unit =
    runReadyOrError(containerId, Map[String, String]())

  /** Launch the app and attach it to the given element. Can be called from Scala or JavaScript.
    */
  @JSExport
  def launch(node: Element): Unit =
    ready(node, Map[String, String]())

  /** Launch the app and attach it to an element with the given id, with the supplied simple flags. Can be called from
    * Scala or JavaScript.
    */
  @JSExport
  def launch(containerId: String, flags: scala.scalajs.js.Dictionary[String]): Unit =
    runReadyOrError(containerId, flags.toMap)

  /** Launch the app and attach it to the given element, with the supplied simple flags. Can be called from Scala or
    * JavaScript.
    */
  @JSExport
  def launch(node: Element, flags: scala.scalajs.js.Dictionary[String]): Unit =
    ready(node, flags.toMap)

  /** Launch the app and attach it to an element with the given id, with the supplied simple flags. Can only be called
    * from Scala.
    */
  def launch(containerId: String, flags: Map[String, String]): Unit =
    runReadyOrError(containerId, flags)

  /** Launch the app and attach it to the given element, with the supplied simple flags. Can only be called from Scala.
    */
  def launch(node: Element, flags: Map[String, String]): Unit =
    ready(node, flags)

  private def routeCurrentLocation(router: Location => GlobalMsg): Cmd[IO, GlobalMsg] =
    val task =
      IO.delay {
        Location.fromJsLocation(window.location)
      }
    Cmd.Run(task, router)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _init(flags: Map[String, String]): (Model, Cmd[IO, GlobalMsg]) =
    init(flags) match
      case Outcome.Result(state, actions) =>
        (state, Action.Many(actions).toCmd |+| routeCurrentLocation(router))

      case e @ Outcome.Error(err, _) =>
        println(e.reportCrash)
        throw err

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def _update(
      model: Model
  ): GlobalMsg => (Model, Cmd[IO, GlobalMsg]) =
    case msg =>
      update(model)(msg) match
        case Outcome.Result(state, actions) =>
          state -> Action.Many(actions).toCmd

        case e @ Outcome.Error(err, _) =>
          println(e.reportCrash)
          throw err

  private def _view(model: Model): Html[GlobalMsg] =
    view(model).toHtml

  private def onUrlChange(router: Location => GlobalMsg): Watch =
    def makeMsg = Option(router(Location.fromJsLocation(window.location)))
    Watch.Batch(
      Watch.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Watch.fromEvent("popstate", window)(_ => makeMsg)
    )

  private def _subscriptions(model: Model): Sub[IO, GlobalMsg] =
    (onUrlChange(router) |+| watchers(model)).toSub

  def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      TyrianApp.start[IO, Model, GlobalMsg](
        node,
        router,
        _init(flags),
        _update,
        _view,
        _subscriptions
      )
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runReadyOrError(containerId: String, flags: Map[String, String]): Unit =
    Option(document.getElementById(containerId)) match
      case Some(e) =>
        ready(e, flags)

      case None =>
        throw new Exception(s"Missing Element! Could not find an element with id '$containerId' on the page.")
