package tyrian

import cats.effect.kernel.Async
import cats.effect.kernel.Ref
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import fs2.Stream
import fs2.concurrent.Channel
import org.scalajs.dom.Element
import snabbdom.VNode
import tyrian.runtime.TyrianRuntime
import tyrian.runtime.TyrianSSR

object Tyrian:

  type Event            = org.scalajs.dom.Event
  type KeyboardEvent    = org.scalajs.dom.KeyboardEvent
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
      init: (Model, Cmd[F, Msg]),
      update: Model => Msg => (Model, Cmd[F, Msg]),
      view: Model => Html[Msg],
      subscriptions: Model => Sub[F, Msg]
  ): Resource[F, TyrianRuntime[F, Model, Msg]] =
    Dispatcher[F].evalMap { dispatcher =>
      for {
        channel <- Channel.synchronous[F, F[Unit]]
        model   <- Async[F].ref(init._1)
        vnode   <- Async[F].ref[Option[VNode]](None)

        runtime <- Async[F].delay {
          new TyrianRuntime(
            init,
            update,
            view,
            subscriptions,
            node,
            model,
            vnode,
            channel,
            dispatcher
          )
        }

      } yield Stream
        .emit(runtime)
        .concurrently(channel.stream.flatMap(Stream.eval))
        .compile
        .resource
        .lastOrError
    }.flatten

  /** Takes a normal Tyrian Model and view function and renders the html to a string prefixed with the doctype.
    */
  def render[Model, Msg](includeDocType: Boolean, model: Model, view: Model => Html[Msg]): String =
    TyrianSSR.render(includeDocType, model, view)

  /** Takes a normal Tyrian Model and view function and renders the html to a string.
    */
  def render[Model, Msg](model: Model, view: Model => Html[Msg]): String =
    render(false, model, view)

  /** Takes a Tyrian HTML view, and renders it into to a string prefixed with the doctype.
    */
  def render[Model, Msg](includeDocType: Boolean, html: Html[Msg]): String =
    TyrianSSR.render(includeDocType, html)

  /** Takes a Tyrian HTML view, and renders it into to a string.
    */
  def render[Model, Msg](html: Html[Msg]): String =
    render(false, html)

  /** Takes a list of Tyrian elements, and renders the fragment into to a string prefixed with the doctype.
    */
  def render[Model, Msg](includeDocType: Boolean, elems: List[Elem[Msg]]): String =
    TyrianSSR.render(includeDocType, elems)

  /** Takes a list of Tyrian elements, and renders the fragment into to a string.
    */
  def render[Model, Msg](elems: List[Elem[Msg]]): String =
    render(false, elems)

  /** Takes repeatingTyrian elements, and renders the fragment into to a string prefixed with the doctype.
    */
  def render[Model, Msg](includeDocType: Boolean, elems: Elem[Msg]*): String =
    render(includeDocType, elems.toList)

  /** Takes repeating Tyrian elements, and renders the fragment into to a string.
    */
  def render[Model, Msg](elems: Elem[Msg]*): String =
    render(elems.toList)
