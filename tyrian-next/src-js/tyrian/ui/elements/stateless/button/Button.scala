package tyrian.ui.elements.stateless.button

import tyrian.Style
import tyrian.next.GlobalMsg
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

import scala.annotation.nowarn

final case class Button(
    isDisabled: Boolean,
    kind: ButtonKind,
    onClick: GlobalMsg,
    text: String,
    classNames: Set[String],
    id: Option[String],
    themeOverride: ThemeOverride[ButtonTheme]
) extends UIElement[Button, ButtonTheme] {

  def withDisabled(disabled: Boolean): Button =
    this.copy(isDisabled = disabled)
  def enabled: Button =
    withDisabled(false)
  def disabled: Button =
    withDisabled(true)

  def withButtonKind(kind: ButtonKind): Button =
    this.copy(kind = kind)
  def button: Button =
    withButtonKind(ButtonKind.Button)
  def reset: Button =
    withButtonKind(ButtonKind.Reset)
  def submit: Button =
    withButtonKind(ButtonKind.Submit)
  def default: Button =
    button

  def withText(text: String): Button =
    this.copy(text = text)

  def withClassNames(classes: Set[String]): Button =
    this.copy(classNames = classes)

  def withId(id: String): Button =
    this.copy(id = Option(id))

  def themeLens: Lens[Theme.Default, ButtonTheme] =
    Lens(
      _.elements.button,
      (t, i) => t.withButtonTheme(i)
    )

  def withThemeOverride(value: ThemeOverride[ButtonTheme]): Button =
    this.copy(themeOverride = value)

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Button.toElem(this, summon[Theme])

}

object Button:

  def apply(text: String, onClick: GlobalMsg): Button =
    Button(false, ButtonKind.Button, onClick, text, Set(), None, ThemeOverride.NoOverride)

  import tyrian.EmptyAttribute
  import tyrian.Html.*

  @nowarn
  def toElem(btn: Button, theme: Theme): tyrian.Elem[GlobalMsg] =
    val disabledAttr =
      if btn.isDisabled then attribute("disabled", "true")
      else EmptyAttribute

    val styles =
      theme match
        case Theme.None =>
          Style.empty

        case Theme.Default(_, elements) =>
          elements.button.toStyles(theme)

    val classAttribute =
      if btn.classNames.isEmpty then EmptyAttribute
      else cls := btn.classNames.mkString(" ")

    val idAttribute =
      btn.id.fold(EmptyAttribute)(id.:=.apply)

    val buttonAttrs = List(
      typ := btn.kind.toAttributeValue,
      disabledAttr,
      style(styles),
      classAttribute,
      idAttribute,
      onClick(btn.onClick)
    )

    button(buttonAttrs)(btn.text)
