package tyrian.ui.button

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class Button[Msg](
    label: String,
    onClick: Msg,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Button[?], Msg]:

  def withLabel(value: String): Button[Msg] =
    this.copy(label = value)

  def withOnClick(value: Msg): Button[Msg] =
    this.copy(onClick = value)

  def withClassNames(classes: Set[String]): Button[Msg] =
    this.copy(classNames = classes)

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
    Button("", onClick, Set(), None)

  def toHtml[Msg](element: Button[Msg])(using theme: Theme): Html[Msg] =
    val t = element._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val classAttribute =
      if element.classNames.isEmpty then EmptyAttribute
      else cls := element.classNames.mkString(" ")

    button(
      style(t.button.toStyles(t)),
      classAttribute,
      onClick(element.onClick)
    )(element.label)
