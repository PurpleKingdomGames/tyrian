package tyrian.ui.elements.stateful.input

import tyrian.Elem
import tyrian.EmptyAttribute
import tyrian.Html.*
import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui
import tyrian.ui.UIElement
import tyrian.ui.UIKey
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing
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
    val h =
      overrideLocalTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(overrideLocalTheme = Some(h))

  def overrideInputTheme(f: InputTheme => InputTheme): Input =
    val g: Theme => Theme = theme => theme.copy(input = f(theme.input))
    withThemeOverride(g)

  def withFontSize(size: FontSize): Input =
    overrideInputTheme(_.withFontSize(size))

  def withFontWeight(weight: FontWeight): Input =
    overrideInputTheme(_.withFontWeight(weight))

  def withTextColor(color: RGBA): Input =
    overrideInputTheme(_.withTextColor(color))

  def withBackgroundColor(color: RGBA): Input =
    overrideInputTheme(_.withBackgroundColor(color))

  def withBorder(border: Border): Input =
    overrideInputTheme(_.withBorder(border))
  def noBorder: Input =
    overrideInputTheme(_.noBorder)
  def modifyBorder(f: Border => Border): Input =
    overrideInputTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Input =
    overrideInputTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Input =
    overrideInputTheme(_.dashedBorder(width, color))

  def withBorderColor(color: RGBA): Input =
    overrideInputTheme(_.modifyBorder(_.withColor(color)))

  def withBorderRadius(radius: BorderRadius): Input =
    overrideInputTheme(_.withBorderRadius(radius))

  def square: Input =
    overrideInputTheme(_.square)
  def rounded: Input =
    overrideInputTheme(_.rounded)
  def roundedSmall: Input =
    overrideInputTheme(_.roundedSmall)
  def roundedLarge: Input =
    overrideInputTheme(_.roundedLarge)
  def circular: Input =
    overrideInputTheme(_.circular)

  def withPadding(padding: Spacing): Input =
    overrideInputTheme(_.withPadding(padding))

  def withDisabledBackgroundColor(value: RGBA): Input =
    overrideInputTheme(_.withDisabledBackgroundColor(value))

  def withDisabledTextColor(value: RGBA): Input =
    overrideInputTheme(_.withDisabledTextColor(value))

  def withDisabledBorderColor(value: RGBA): Input =
    overrideInputTheme(_.withDisabledBorderColor(value))

  def update: GlobalMsg => Outcome[Input] =
    case TextInputMsg.Changed(_key, v) if _key == key =>
      Outcome(this.copy(value = v))

    case TextInputMsg.Clear(_key) if _key == key =>
      Outcome(this.copy(value = ""))

    case _ =>
      Outcome(this)

  def view: Theme ?=> Elem[GlobalMsg] =
    val theme = summon[Theme]

    val inputTheme = overrideLocalTheme match
      case Some(f) => f(theme).input
      case None    => theme.input

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
