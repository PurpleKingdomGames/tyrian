package tyrian.ui.input

import tyrian.Elem
import tyrian.EmptyAttribute
import tyrian.Html.*
import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.UIElementId

final case class TextInput(
    id: UIElementId,
    state: TextInput.State,
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
) extends UIElement.Stateful[TextInput]:

  def value: String =
    state.value

  def withPlaceholder(placeholder: String): TextInput =
    this.copy(state = state.withPlaceholder(placeholder))

  def withDisabled(disabled: Boolean): TextInput =
    this.copy(state = state.withDisabled(disabled))
  def enable: TextInput =
    withDisabled(false)
  def disable: TextInput =
    withDisabled(true)

  def withReadonly(readOnly: Boolean): TextInput =
    this.copy(state = state.withReadonly(readOnly))
  def makeReadonly: TextInput =
    withReadonly(true)
  def makeEditable: TextInput =
    withReadonly(false)

  def withValue(value: String): TextInput =
    this.copy(state = state.withValue(value))

  def withId(value: UIElementId): TextInput =
    this.copy(id = value)

  def withState(next: TextInput.State): TextInput =
    this.copy(state = next)

  def withClassNames(classes: Set[String]): TextInput =
    this.copy(classNames = classes)

  def withThemeOverride(f: Theme => Theme): TextInput =
    this.copy(overrideLocalTheme = Some(f))

  def update: GlobalMsg => Outcome[TextInput] =
    case TextInput.Msg.Changed(_id, v) if _id == id =>
      Outcome(withState(state.copy(value = v)))

    case TextInput.Msg.Clear(_id) if _id == id =>
      Outcome(withState(state.copy(value = "")))

    case _ =>
      Outcome(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    val disabledAttr = if state.disabled then attribute("disabled", "true") else EmptyAttribute
    val readonlyAttr = if state.readonly then attribute("readonly", "true") else EmptyAttribute

    val inputAttrs = List(
      placeholder       := state.placeholder,
      tyrian.Html.value := state.value,
      typ               := "text",
      onInput((s: String) => TextInput.Msg.Changed(id, s)),
      disabledAttr,
      readonlyAttr
    )

    input(inputAttrs*)

object TextInput:

  enum Msg extends GlobalMsg:
    case Changed(id: UIElementId, value: String)
    case Clear(id: UIElementId)

  final case class State(
      placeholder: String,
      disabled: Boolean,
      readonly: Boolean,
      value: String
  ) derives CanEqual:

    def withPlaceholder(p: String): State =
      this.copy(placeholder = p)

    def withDisabled(d: Boolean): State =
      this.copy(disabled = d)

    def withReadonly(r: Boolean): State =
      this.copy(readonly = r)

    def withValue(v: String): State =
      this.copy(value = v)

  object State:
    val default: State =
      State(
        placeholder = "",
        disabled = false,
        readonly = false,
        value = ""
      )

  def apply(id: UIElementId): TextInput =
    TextInput(id, State.default, Set.empty, None)
