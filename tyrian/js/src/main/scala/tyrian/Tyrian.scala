package tyrian

import cats.effect.kernel.Async
import org.scalajs.dom.Element
import tyrian.runtime.TyrianRuntime

object Tyrian:

  type Event            = org.scalajs.dom.Event
  type KeyboardEvent    = org.scalajs.dom.KeyboardEvent
  type MouseEvent       = org.scalajs.dom.MouseEvent
  type HTMLInputElement = org.scalajs.dom.HTMLInputElement

  /** Directly starts the app. Computes the initial state of the given application, renders it on the given DOM element,
    * and listens to user actions
    * @param init
    *   initial state
    * @param update
    *   state transition function
    * @param view
    *   view function
    * @param subscriptions
    *   subscriptions function
    * @param node
    *   the DOM element to mount the app to
    * @param runner
    *   the function that runs the program. Has a type of `F[Option[Msg]] => (Either[Throwable, Option[Msg]] => Unit) =>
    *   Unit`, essentially: `task.unsafeRunAsync(callback)`
    * @tparam F
    *   The effect type to use, e.g. `IO`
    * @tparam Model
    *   Type of model
    * @tparam Msg
    *   Type of messages
    */
  def start[F[_]: Async, Model, Msg](
      node: Element,
      router: Location => Msg,
      init: (Model, Cmd[F, Msg]),
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[F, Msg]
  ): F[Nothing] =
    TyrianRuntime[F, Model, Msg](router, node, init._1, init._2, update, view, subscriptions)
