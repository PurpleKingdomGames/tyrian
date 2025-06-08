package tyrian.ui

import tyrian.ui.theme.TextTheme

final case class Text(value: String, _modifyTheme: Option[Theme => Theme]) extends UIElement[Nothing]:
  type T = Text

  def withValue(value: String): Text =
    this.copy(value = value)

  def modifyTheme(f: Theme => Theme): Text =
    this.copy(_modifyTheme = Some(f))

  def modifyTextTheme(f: TextTheme => TextTheme): Text =
    val g: Theme => Theme = theme => theme.withTextTheme(f(theme.text))
    this.copy(_modifyTheme = Some(g))

  def toHtml: Theme ?=> tyrian.Html[Nothing] =
    Text.toHtml(this)

object Text:

  import tyrian.Html
  import tyrian.Html.*

  def apply(value: String): Text =
    Text(value, None)

  def toHtml(element: Text)(using theme: Theme): Html[Nothing] =
    val t = element._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    span(
      style(t.text.toStyles(t))
    )(element.value)
