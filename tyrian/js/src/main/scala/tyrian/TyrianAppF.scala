package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.DocumentReadyState
import org.scalajs.dom.Element
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.runtime.TyrianRuntime

import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.*

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianAppF[F[_]: Async, Msg, Model]:

  /** Specifies the number of queued tasks that can be consumed at any one time. Default is 1024 which is assumed to be
    * more than sufficient, however the value can be tweaked in your app by overriding this value.
    */
  def MaxConcurrentTasks: Int = 1024

  val run: Resource[F, TyrianRuntime[F, Model, Msg]] => Unit

  def router: Location => Msg

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

  private def routeCurrentLocation[F[_]: Async, Msg](router: Location => Msg): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        Location.fromJsLocation(window.location)
      }
    Cmd.Run(task, router)

  private def _init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(flags)
    (m, cmd |+| routeCurrentLocation[F, Msg](router))

  private def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  private def _view(model: Model): Html[Msg] =
    view(model)

  private def onUrlChange[F[_]: Async, Msg](router: Location => Msg): Sub[F, Msg] =
    def makeMsg = Option(router(Location.fromJsLocation(window.location)))
    Sub.Batch(
      Sub.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Sub.fromEvent("popstate", window)(_ => makeMsg)
    )

  private def _subscriptions(model: Model): Sub[F, Msg] =
    onUrlChange[F, Msg](router) |+| subscriptions(model)

  def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      Tyrian.start(
        node,
        router,
        _init(flags),
        _update,
        _view,
        _subscriptions,
        MaxConcurrentTasks
      )
    )

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  private def runReadyOrError(containerId: String, flags: Map[String, String]): Unit =
    Option(document.getElementById(containerId)) match
      case Some(e) =>
        ready(e, flags)

      case None =>
        throw new Exception(s"Missing Element! Could not find an element with id '$containerId' on the page.")

object TyrianAppF:
  /** Launch app instances after DOMContentLoaded.
    */
  def onLoad[F[_] : Async](appDirectory: Map[String, TyrianAppF[F, _, _]]): Unit =
    val documentReady = new Promise((resolve, _reject) => {
      document.addEventListener("DOMContentLoaded", _ => {
        resolve(())
      })
      if (document.readyState != DocumentReadyState.loading) {
        resolve(())
      }
    })
    documentReady.`then`(_ => {
      launch[F](appDirectory)
    })

  def onLoad[F[_] : Async](appDirectory: (String, TyrianAppF[F, _, _])*): Unit =
    onLoad(appDirectory.toMap)

  /** Find data-tyrian-app HTMLElements and launch corresponding TyrianAppF instances
    */
  def launch[F[_] : Async](appDirectory: Map[String, TyrianAppF[F, _, _]]): Unit =
    for {
      element <- document.querySelectorAll("[data-tyrian-app]")
    } yield {
      val tyrianAppElement = element.asInstanceOf[HTMLElement]
      val tyrianAppName = tyrianAppElement.dataset.get("tyrianApp")
      val appSupplierOption = for {
        appName <- tyrianAppName
        appSupplier <- appDirectory.get(appName)
      } yield appSupplier
      appSupplierOption match
        case Some(appSupplier) =>
          appSupplier.launch(tyrianAppElement, appElementFlags(tyrianAppElement))
        case _ =>
          println(s"Could not find an app entry for ${tyrianAppName.getOrElse("")}")
    }

  private def appElementFlags(tyrianAppElement: HTMLElement): Map[String,String] =
    val appFlags = for {
      (dataAttr, attrValue) <- tyrianAppElement.dataset
      if dataAttr.startsWith("tyrianFlag")
      flagName = dataAttr.replaceFirst("^tyrianFlag", "")
    } yield (flagName, attrValue)
    appFlags.toMap