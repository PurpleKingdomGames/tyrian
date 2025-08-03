package tyrian.ui.button

import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class Button[Msg](
    label: String,
    onClick: Msg,
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Msg]:
  type T = Button[Msg]

  def withLabel(value: String): Button[Msg] =
    this.copy(label = value)

  def withOnClick(value: Msg): Button[Msg] =
    this.copy(onClick = value)

  def modifyTheme(f: Theme => Theme): Button[Msg] =
    this.copy(_modifyTheme = Some(f))

  def modifyTextTheme(f: ButtonTheme => ButtonTheme): Button[Msg] =
    val g: Theme => Theme = theme => theme.withButtonTheme(f(theme.button))
    this.copy(_modifyTheme = Some(g))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Button.toHtml(this)

object Button:

  import tyrian.Html
  import tyrian.Html.*

  def apply[Msg](onClick: Msg): Button[Msg] =
    Button("", onClick, None)

  def toHtml[Msg](element: Button[Msg])(using theme: Theme): Html[Msg] =
    val t = element._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    button(
      style(t.button.toStyles(t)),
      onClick(element.onClick)
    )(element.label)
