package tyrian

object Tyrian:

  final case class FakeEvent(name: String, value: Any, target: Any)
  final case class FakeHTMLInputElement(value: String)
  type Event            = FakeEvent
  type KeyboardEvent    = FakeEvent
  type MouseEvent       = FakeEvent
  type HTMLInputElement = FakeHTMLInputElement
