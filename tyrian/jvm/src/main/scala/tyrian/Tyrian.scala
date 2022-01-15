package tyrian

import tyrian.runtime.TyrianSSR

object Tyrian:

  final case class FakeEvent(name: String, value: Any, target: Any)
  final case class FakeHTMLInputElement(value: String)
  type Event = FakeEvent
  type KeyboardEvent = FakeEvent
  type HTMLInputElement = FakeHTMLInputElement

  /** Takes a normal Tyrian Model and view function and renders the html to a string.
    */
  def render[Model, Msg](model: Model, view: Model => Html[Msg]): String =
    TyrianSSR.render(model, view)

  /** Takes a Tyrian HTML view, and renders it into to a string.
    */
  def render[Model, Msg](html: Html[Msg]): String =
    TyrianSSR.render(html)
