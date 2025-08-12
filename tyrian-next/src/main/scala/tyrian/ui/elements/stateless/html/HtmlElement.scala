package tyrian.ui.elements.stateless.html

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.utils.Lens

final case class HtmlElement(
    html: tyrian.Elem[GlobalMsg],
    classNames: Set[String],
    themeOverride: Option[Unit => Unit]
) extends UIElement[HtmlElement, Unit]:

  def withHtml(html: tyrian.Elem[GlobalMsg]): HtmlElement =
    this.copy(html = html)

  def withClassNames(classes: Set[String]): HtmlElement =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme.Styles, Unit] =
    Lens.unit

  def withThemeOverride(f: Unit => Unit): HtmlElement =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    HtmlElement.toHtml(this)

object HtmlElement:

  import tyrian.Html
  import tyrian.Html.*

  def apply(html: Html[GlobalMsg]): HtmlElement =
    HtmlElement(
      html = html,
      classNames = Set.empty,
      themeOverride = None
    )

  def raw(htmlString: String): HtmlElement =
    HtmlElement(
      html = div().innerHtml(htmlString),
      classNames = Set.empty,
      themeOverride = None
    )

  def text(content: String): HtmlElement =
    HtmlElement(
      html = span(content),
      classNames = Set.empty,
      themeOverride = None
    )

  def toHtml(element: HtmlElement): tyrian.Elem[GlobalMsg] =
    element.html
