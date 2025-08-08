package tyrian.ui.button

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class Button(
    label: String,
    onClick: GlobalMsg,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Button]:

  def withLabel(value: String): Button =
    this.copy(label = value)

  def withOnClick(value: GlobalMsg): Button =
    this.copy(onClick = value)

  def withClassNames(classes: Set[String]): Button =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Button =
    this.copy(_modifyTheme = Some(f))

  def modifyTextTheme(f: ButtonTheme => ButtonTheme): Button =
    val g: Theme => Theme = theme => theme.withButtonTheme(f(theme.button))
    this.copy(_modifyTheme = Some(g))

  def toHtml: Theme ?=> tyrian.Elem[GlobalMsg] =
    Button.toHtml(this)

object Button:

  import tyrian.Html.*

  def apply(onClick: GlobalMsg): Button =
    Button("", onClick, Set(), None)

  def toHtml(element: Button)(using theme: Theme): tyrian.Elem[GlobalMsg] =
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
