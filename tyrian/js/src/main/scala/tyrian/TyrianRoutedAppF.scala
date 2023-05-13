package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.Element
import org.scalajs.dom.PopStateEvent
import org.scalajs.dom.document
import org.scalajs.dom.window

import scala.scalajs.js
import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianRoutedAppF[F[_]: Async, Msg, Model] extends TyrianAppF[F, Msg, Model]:

  def router: Location => Msg

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

  private def _subscriptions(model: Model): Sub[F, Msg] =
    Routing.onUrlChange[F, Msg](router) |+| subscriptions(model)

  override def ready(node: Element, flags: Map[String, String]): Unit =
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

object Routing:

  def onUrlChange[F[_]: Async, Msg](router: Location => Msg): Sub[F, Msg] =
    def makeMsg = Option(router(Location.fromJsLocation(window.location)))
    Sub.Batch(
      Sub.fromEvent("DOMContentLoaded", window)(_ => makeMsg),
      Sub.fromEvent("popstate", window)(_ => makeMsg)
    )

object Nav:

  def pushUrl[F[_]: Async](url: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.history.pushState("", "", url)
    }

  def loadUrl[F[_]: Async](href: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.location.href = href
    }
