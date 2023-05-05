package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.scalajs.dom.Element
import org.scalajs.dom.Location
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.runtime.TyrianRuntime

import scala.scalajs.js
import scala.scalajs.js.annotation._

/** The TyrianApp trait can be extended to conveniently prompt you for all the methods needed for a Tyrian app, as well
  * as providing a number of standard app launching methods.
  */
trait TyrianRoutedAppF[F[_]: Async, Msg, Model] extends TyrianAppF[F, Msg, Model]:

  def router: String => Msg

  def setLocationHash(newHash: String): Cmd[F, Nothing] =
    Routing.setLocation(newHash)

  private def _init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(flags)
    (m, Routing.getLocation[F, Msg](router) |+| cmd)

  private def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  private def _view(model: Model): Html[Msg] =
    view(model)

  private def _subscriptions(model: Model): Sub[F, Msg] =
    Routing.onLocationChange[F, Msg](router) |+| subscriptions(model)

  override def ready(node: Element, flags: Map[String, String]): Unit =
    run(
      Tyrian.start(
        node,
        _init(flags),
        _update,
        _view,
        _subscriptions,
        MaxConcurrentTasks
      )
    )

object Routing:

  private def locationToRoute(loc: Location): String =
    val origin = loc.origin.getOrElse("")
    loc.toString.replaceFirst(origin, "")

  def onLocationChange[F[_]: Async, Msg](router: String => Msg): Sub[F, Msg] =
    Sub.Batch(
      Sub.fromEvent("DOMContentLoaded", window) { _ =>
        Option((locationToRoute andThen router)(window.location))
      },
      Sub.fromEvent("popstate", window) { _ =>
        Option((locationToRoute andThen router)(window.location))
      }
    )

  def getLocation[F[_]: Async, Msg](router: String => Msg): Cmd[F, Msg] =
    val task =
      Async[F].delay {
        locationToRoute(window.location)
      }
    Cmd.Run(task, router)

  def setLocation[F[_]: Async](newHash: String): Cmd[F, Nothing] =
    Cmd.SideEffect {
      window.location.hash = if newHash.startsWith("#") then newHash else "#" + newHash
    }
