package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.Element
import org.scalajs.dom.document
import tyrian.runtime.TyrianRuntime

import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianAppF[F[_]: Async, Msg, Model]:

  /** Specifies the number of queued tasks that can be consumed at any one time. Default is 1024 which is assumed to be
    * more than sufficient, however the value can be tweaked in your app by overriding this value.
    */
  def MaxConcurrentTasks: Int = 1024

  val run: F[Nothing] => Unit

  /** Used to initialise your app. Accepts simple flags and produces the initial model state, along with any commands to
    * run at start up, in order to trigger other processes.
    */
  def init(flags: Map[String, String]): (Model, Cmd[F, Msg])

  /** The update method allows you to modify the model based on incoming messages (events). As well as an updated model,
    * you can also produce commands to run.
    */
  def update(model: Model): Msg => (Model, Cmd[F, Msg])

  /** Used to render your current model into an HTML view.
    */
  def view(model: Model): Html[Msg]

  /** Subscriptions are typically processes that run for a period of time and emit discrete events based on some world
    * event, e.g. a mouse moving might emit it's coordinates.
    */
  def subscriptions(model: Model): Sub[F, Msg]

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

  def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      Tyrian.start[F, Model, Msg](
        node,
        init(flags),
        update,
        view,
        subscriptions,
      )
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runReadyOrError(containerId: String, flags: Map[String, String]): Unit =
    Option(document.getElementById(containerId)) match
      case Some(e) =>
        ready(e, flags)

      case None =>
        throw new Exception(s"Missing Element! Could not find an element with id '$containerId' on the page.")
