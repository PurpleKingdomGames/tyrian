package tyrian.ui.input

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class Input[+Msg](
    value: String,
    onInput: String => Msg,
    placeholder: Option[String],
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Input[?], Msg]:

  def withValue(value: String): Input[Msg] =
    this.copy(value = value)

  def withOnInput[M >: Msg](handler: String => M): Input[M] =
    this.copy(onInput = handler)

  def withPlaceholder(text: String): Input[Msg] =
    this.copy(placeholder = Some(text))

  def withoutPlaceholder: Input[Msg] =
    this.copy(placeholder = None)

  def withClassNames(classes: Set[String]): Input[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Input[Msg] =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Input.toHtml(this)

object Input:

  final case class Model(value: String)

  import tyrian.Html
  import tyrian.Html.*

  def apply[Msg](value: String, onInput: String => Msg): Input[Msg] =
    Input(value, onInput, None, Set(), None)

  def withPlaceholder[Msg](value: String, onInput: String => Msg, placeholder: String): Input[Msg] =
    Input(value, onInput, Some(placeholder), Set(), None)

  def toHtml[Msg](element: Input[Msg]): Html[Msg] =

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
