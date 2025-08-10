package tyrian.ui.elements.stateful.input

import tyrian.Elem
import tyrian.EmptyAttribute
import tyrian.Html.*
import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui
import tyrian.ui.UIElement
import tyrian.ui.UIKey
import tyrian.ui.theme.Theme

final case class Input(
    key: UIKey,
    placeholder: String,
    isDisabled: Boolean,
    isReadOnly: Boolean,
    value: String,
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
) extends UIElement.Stateful[Input]:

  def withPlaceholder(placeholder: String): Input =
    this.copy(placeholder = placeholder)

  def withDisabled(disabled: Boolean): Input =
    this.copy(isDisabled = disabled)
  def enabled: Input =
    withDisabled(false)
  def disabled: Input =
    withDisabled(true)

  def withReadonly(readOnly: Boolean): Input =
    this.copy(isReadOnly = readOnly)
  def readOnly: Input =
    withReadonly(true)
  def editable: Input =
    withReadonly(false)

  def withValue(value: String): Input =
    this.copy(value = value)

  def withKey(value: UIKey): Input =
    this.copy(key = value)

  def withClassNames(classes: Set[String]): Input =
    this.copy(classNames = classes)

  def withThemeOverride(f: Theme => Theme): Input =
    this.copy(overrideLocalTheme = Some(f))

  def update: GlobalMsg => Outcome[Input] =
    case TextInputMsg.Changed(_key, v) if _key == key =>
      Outcome(this.copy(value = v))

    case TextInputMsg.Clear(_key) if _key == key =>
      Outcome(this.copy(value = ""))

    case _ =>
      Outcome(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    val disabledAttr = if isDisabled then attribute("disabled", "true") else EmptyAttribute
    val readonlyAttr = if isReadOnly then attribute("readonly", "true") else EmptyAttribute

    val inputAttrs = List(
      tyrian.Html.placeholder := placeholder,
      tyrian.Html.value       := value,
      typ                     := "text",
      onInput((s: String) => TextInputMsg.Changed(key, s)),
      disabledAttr,
      readonlyAttr
    )

    input(inputAttrs*)

object Input:

  def apply(key: UIKey): Input =
    Input(
      key,
      placeholder = "",
      isDisabled = false,
      isReadOnly = false,
      value = "",
      Set.empty,
      None
    )

enum TextInputMsg extends GlobalMsg:
  case Changed(id: UIKey, value: String)
  case Clear(id: UIKey)
