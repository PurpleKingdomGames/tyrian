package tyrian.ui

final case class Button[Msg](label: String, onClick: Msg) extends UIElement[Msg]:

  def withLabel(value: String): Button[Msg] =
    this.copy(label = value)

  def withOnClick(value: Msg): Button[Msg] =
    this.copy(onClick = value)

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Button.toHtml(this)

object Button:

  import tyrian.Html
  import tyrian.Html.*

  def apply[Msg](onClick: Msg): Button[Msg] =
    Button("", onClick)

  def toHtml[Msg](element: Button[Msg])(using theme: Theme): Html[Msg] =
    button(
      style(theme.button.toStyles),
      onClick(element.onClick)
    )(element.label)
