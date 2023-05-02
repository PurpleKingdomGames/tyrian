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
trait TyrianRoutedAppF[F[_]: Async, Msg, Model] extends TyrianAppF[F, Msg, Model]:

  def hashResultToMessage: Navigation.Result => Msg
  def hashChangeToMessage: Navigation.Result.HashChange => Msg

  def setLocationHash(newHash: String): Cmd[F, Nothing] =
    Navigation.setLocationHash(newHash)

  def _init(flags: Map[String, String]): (Model, Cmd[F, Msg]) =
    val (m, cmd) = init(flags)
    (m, Navigation.getLocationHash[F, Msg](hashResultToMessage) |+| cmd)

  def _update(model: Model): Msg => (Model, Cmd[F, Msg]) =
    msg => update(model)(msg)

  def _view(model: Model): Html[Msg] =
    view(model)

  def _subscriptions(model: Model): Sub[F, Msg] =
    Navigation.onLocationHashChange[F, Msg](hashChangeToMessage) |+| subscriptions(model)

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
