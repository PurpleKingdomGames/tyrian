package tyrian.ui.elements.stateful.input

import tyrian.Elem
import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui
import tyrian.ui.UIElement
import tyrian.ui.UIKey
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Input(
    placeholder: String,
    isDisabled: Boolean,
    isReadOnly: Boolean,
    value: String,
    key: UIKey,
    classNames: Set[String],
    id: Option[String],
    themeOverride: ThemeOverride[InputTheme]
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

  def withId(id: String): Input =
    this.copy(id = Some(id))

  def themeLens: Lens[Theme.Default, InputTheme] =
    Lens(
      _.elements.input,
      (t, i) => t.withInputTheme(i)
    )

  def withThemeOverride(value: ThemeOverride[InputTheme]): Input =
    this.copy(themeOverride = value)

  def update: GlobalMsg => Outcome[Input] =
    case TextInputMsg.Changed(_key, v) if _key == key =>
      Outcome(this.copy(value = v))

    case TextInputMsg.Clear(_key) if _key == key =>
      Outcome(this.copy(value = ""))

    case _ =>
      Outcome(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    Input.toHtml(this)

object Input:

  import tyrian.Html.*
  import tyrian.Style

  def apply(key: UIKey): Input =
    Input(
      placeholder = "",
      isDisabled = false,
      isReadOnly = false,
      value = "",
      key,
      Set.empty,
      id = None,
      ThemeOverride.NoOverride
    )

  def toHtml(i: Input)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val disabledAttr =
      if i.isDisabled then attribute("disabled", "true")
      else EmptyAttribute

    val readonlyAttr =
      if i.isReadOnly then attribute("readonly", "true")
      else EmptyAttribute

    val styles =
      theme match
        case Theme.None =>
          Style.empty

        case tt: Theme.Default =>
          if i.isDisabled then tt.elements.input.toDisabledStyles(theme)
          else tt.elements.input.toStyles(theme)

    val classAttribute =
      if i.classNames.isEmpty then EmptyAttribute
      else cls := i.classNames.mkString(" ")

    val idAttribute =
      i.id.fold(EmptyAttribute)(id.:=.apply)

    val inputAttrs = List(
      tyrian.Html.placeholder := i.placeholder,
      tyrian.Html.value       := i.value,
      typ                     := "text",
      onInput((s: String) => TextInputMsg.Changed(i.key, s)),
      disabledAttr,
      readonlyAttr,
      style(styles),
      classAttribute,
      idAttribute
    )

    input(inputAttrs*)

enum TextInputMsg extends GlobalMsg:
  case Changed(id: UIKey, value: String)
  case Clear(id: UIKey)
