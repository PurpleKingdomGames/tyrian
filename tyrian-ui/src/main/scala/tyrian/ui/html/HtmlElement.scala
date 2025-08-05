package tyrian.ui.html

import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class HtmlElement[+Msg](
    html: tyrian.Html[Msg],
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[HtmlElement[?], Msg]:

  def withHtml[LubMsg >: Msg](html: tyrian.Html[LubMsg]): HtmlElement[LubMsg] =
    this.copy(html = html)

  def withClassNames(classes: Set[String]): HtmlElement[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): HtmlElement[Msg] =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    HtmlElement.toHtml(this)

object HtmlElement:

  import tyrian.Html
  import tyrian.Html.*

  def apply[Msg](html: Html[Msg]): HtmlElement[Msg] =
    HtmlElement(
      html = html,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def raw[Msg](htmlString: String): HtmlElement[Msg] =
    HtmlElement(
      html = div().innerHtml(htmlString),
      classNames = Set.empty,
      _modifyTheme = None
    )

  def text[Msg](content: String): HtmlElement[Msg] =
    HtmlElement(
      html = span(content),
      classNames = Set.empty,
      _modifyTheme = None
    )

  def toHtml[Msg](element: HtmlElement[Msg]): Html[Msg] =
    element.html
