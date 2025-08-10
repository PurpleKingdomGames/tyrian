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
import tyrian.ui.utils.Lens

final case class Input(
    key: UIKey,
    placeholder: String,
    isDisabled: Boolean,
    isReadOnly: Boolean,
    value: String,
    classNames: Set[String],
    themeOverride: Option[InputTheme => InputTheme]
) extends UIElement.Stateful[Input, InputTheme]:

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

  def themeLens: Lens[Theme, InputTheme] =
    Lens(
      _.input,
      (t, i) => t.copy(input = i)
    )

  def withThemeOverride(f: InputTheme => InputTheme): Input =
    this.copy(themeOverride = Some(f))

  def update: GlobalMsg => Outcome[Input] =
    case TextInputMsg.Changed(_key, v) if _key == key =>
      Outcome(this.copy(value = v))

    case TextInputMsg.Clear(_key) if _key == key =>
      Outcome(this.copy(value = ""))

    case _ =>
      Outcome(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    val theme      = summon[Theme]
    val inputTheme = theme.input

    val disabledAttr =
      if isDisabled then attribute("disabled", "true")
      else EmptyAttribute

    val readonlyAttr =
      if isReadOnly then attribute("readonly", "true")
      else EmptyAttribute

    val styles =
      if isDisabled then inputTheme.toDisabledStyles(theme)
      else inputTheme.toStyles(theme)

    val classAttribute =
      if classNames.isEmpty then EmptyAttribute
      else cls := classNames.mkString(" ")

    val inputAttrs = List(
      tyrian.Html.placeholder := placeholder,
      tyrian.Html.value       := value,
      typ                     := "text",
      onInput((s: String) => TextInputMsg.Changed(key, s)),
      disabledAttr,
      readonlyAttr,
      style(styles),
      classAttribute
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
