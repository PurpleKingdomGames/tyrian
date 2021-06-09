package tyrian

import tyrian.runtime.TyrianRuntime
import org.scalajs.dom.Element

object Tyrian:

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
