package tyrian

import tyrian.runtime.TyrianSSR

object Tyrian:

  final case class FakeEvent(name: String, value: Any, target: Any)
  final case class FakeHTMLInputElement(value: String)
  type Event            = FakeEvent
  type KeyboardEvent    = FakeEvent
  type MouseEvent       = FakeEvent
  type HTMLInputElement = FakeHTMLInputElement

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
