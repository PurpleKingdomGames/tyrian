package tyrian.ui.html

import tyrian.next.GlobalMsg
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class HtmlElement(
    html: tyrian.Elem[GlobalMsg],
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
) extends UIElement[HtmlElement]:

  def withHtml(html: tyrian.Elem[GlobalMsg]): HtmlElement =
    this.copy(html = html)

  def withClassNames(classes: Set[String]): HtmlElement =
    this.copy(classNames = classes)

  def withThemeOverride(f: Theme => Theme): HtmlElement =
    this.copy(overrideLocalTheme = Some(f))

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    HtmlElement.toHtml(this)

object HtmlElement:

  import tyrian.Html
  import tyrian.Html.*

  def apply(html: Html[GlobalMsg]): HtmlElement =
    HtmlElement(
      html = html,
      classNames = Set.empty,
      overrideLocalTheme = None
    )

  def raw(htmlString: String): HtmlElement =
    HtmlElement(
      html = div().innerHtml(htmlString),
      classNames = Set.empty,
      overrideLocalTheme = None
    )

  def text(content: String): HtmlElement =
    HtmlElement(
      html = span(content),
      classNames = Set.empty,
      overrideLocalTheme = None
    )

  def toHtml(element: HtmlElement): tyrian.Elem[GlobalMsg] =
    element.html
