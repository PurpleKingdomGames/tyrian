package tyrian.ui.input

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class Input(
    value: String,
    onInput: String => GlobalMsg,
    placeholder: Option[String],
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Input]:

  def withValue(value: String): Input =
    this.copy(value = value)

  def withOnInput(handler: String => GlobalMsg): Input =
    this.copy(onInput = handler)

  def withPlaceholder(text: String): Input =
    this.copy(placeholder = Some(text))

  def withoutPlaceholder: Input =
    this.copy(placeholder = None)

  def withClassNames(classes: Set[String]): Input =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Input =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> tyrian.Elem[GlobalMsg] =
    Input.toHtml(this)

object Input:

  final case class Model(value: String)

  import tyrian.Html.*

  def apply(value: String, onInput: String => GlobalMsg): Input =
    Input(value, onInput, None, Set(), None)

  def withPlaceholder(value: String, onInput: String => GlobalMsg, placeholder: String): Input =
    Input(value, onInput, Some(placeholder), Set(), None)

  def toHtml(element: Input): tyrian.Elem[GlobalMsg] =

    val classAttribute =
      if element.classNames.isEmpty then EmptyAttribute
      else cls := element.classNames.mkString(" ")

    val placeholderAttribute = element.placeholder match
      case Some(text) => placeholder := text
      case None       => EmptyAttribute

    input(
      tpe   := "text",
      value := element.value,
      onInput(element.onInput),
      placeholderAttribute,
      classAttribute
    )
