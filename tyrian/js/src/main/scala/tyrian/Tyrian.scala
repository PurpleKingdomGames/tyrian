package tyrian

import org.scalajs.dom.Element
import tyrian.runtime.TyrianRuntime
import tyrian.runtime.TyrianSSR

object Tyrian:

  type Event = org.scalajs.dom.Event
  type KeyboardEvent = org.scalajs.dom.KeyboardEvent
  type HTMLInputElement = org.scalajs.dom.raw.HTMLInputElement

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
  ): Unit =
    new TyrianRuntime(
      (init, Cmd.Empty),
      (msg: Msg, m: Model) => (update(msg, m), Cmd.Empty),
      view,
      _ => Sub.Empty,
      node
    ).start()

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
  ): Unit =
    new TyrianRuntime(
      init,
      update,
      view,
      subscriptions,
      node
    ).start()

  /** Takes a normal Tyrian Model and view function and renders the html to a string.
    */
  def render[Model, Msg](model: Model, view: Model => Html[Msg]): String =
    TyrianSSR.render(model, view)

  /** Takes a Tyrian HTML view, and renders it into to a string.
    */
  def render[Model, Msg](html: Html[Msg]): String =
    TyrianSSR.render(html)
