package tyrian.ui.elements.stateless.html

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class HtmlElement(
    html: tyrian.Elem[GlobalMsg]
) extends UIElement[HtmlElement, Unit]:

  val classNames: Set[String]            = Set()
  val id: Option[String]                 = None
  val themeOverride: ThemeOverride[Unit] = ThemeOverride.NoOverride

  def withHtml(html: tyrian.Elem[GlobalMsg]): HtmlElement =
    this.copy(html = html)

  def withClassNames(classes: Set[String]): HtmlElement =
    this

  def withId(id: String): HtmlElement =
    this

  def themeLens: Lens[Theme.Default, Unit] =
    Lens.unit

  def withThemeOverride(f: ThemeOverride[Unit]): HtmlElement =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    HtmlElement.toHtml(this)

object HtmlElement:

  import tyrian.Html.*

  def raw(htmlString: String): HtmlElement =
    HtmlElement(html = div().innerHtml(htmlString))

  def text(content: String): HtmlElement =
    HtmlElement(html = span(content))

  def toHtml(element: HtmlElement): tyrian.Elem[GlobalMsg] =
    element.html
