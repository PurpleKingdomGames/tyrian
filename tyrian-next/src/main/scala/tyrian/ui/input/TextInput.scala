package tyrian.ui.input

import tyrian.Elem
import tyrian.Empty
import tyrian.EmptyAttribute
import tyrian.Html.*
import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class TextInput(
    state: TextInput.State,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[TextInput] {

  def withState(next: TextInput.State): TextInput =
    this.copy(state = next)

  def withClassNames(classes: Set[String]): TextInput =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): TextInput =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> Elem[GlobalMsg] = {
    val disabledAttr = if state.disabled then attribute("disabled", "true") else EmptyAttribute
    val readonlyAttr = if state.readonly then attribute("readonly", "true") else EmptyAttribute

    val inputAttrs = List(
      placeholder := state.placeholder,
      value       := state.value,
      typ         := state.kind,
      onInput((s: String) => TextInput.Msg.Changed(state.uid, s)),
      disabledAttr,
      readonlyAttr
    )

    val clearBtn =
      if state.showClearIcon && state.value.nonEmpty then button(onClick(TextInput.Msg.Cleared(state.uid)))(text("×"))
      else Empty

    div()(input(inputAttrs*), clearBtn)
  }
}

object TextInput {

  enum Msg extends GlobalMsg {
    case Changed(uid: String, value: String)
    case Cleared(uid: String)
  }

  final case class State(
      placeholder: String,
      showClearIcon: Boolean,
      disabled: Boolean,
      readonly: Boolean,
      value: String,
      kind: String,
      uid: String
  ) derives CanEqual {

    def withPlaceholder(p: String): State = this.copy(placeholder = p)
    def showClearIconOn: State            = this.copy(showClearIcon = true)
    def showClearIconOff: State           = this.copy(showClearIcon = false)
    def withDisabled(d: Boolean): State   = this.copy(disabled = d)
    def withReadonly(r: Boolean): State   = this.copy(readonly = r)
    def withValue(v: String): State       = this.copy(value = v)
    def withKind(k: String): State        = this.copy(kind = k)
    def withUid(id: String): State        = this.copy(uid = id)

    def update: GlobalMsg => Outcome[State] = {
      case Msg.Changed(id, v) if id == uid => Outcome(this.copy(value = v))
      case Msg.Cleared(id) if id == uid    => Outcome(this.copy(value = ""))
      case _                               => Outcome(this)
    }
  }

  object State {
    val default: State =
      State(
        placeholder = "",
        showClearIcon = true,
        disabled = false,
        readonly = false,
        value = "",
        kind = "text",
        uid = ""
      )

    def apply(uid: String): State =
      default.copy(uid = uid)
  }

  def apply(state: State): TextInput =
    TextInput(state, Set.empty, None)
}
