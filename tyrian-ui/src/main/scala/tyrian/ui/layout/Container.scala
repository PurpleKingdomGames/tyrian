package tyrian.ui.layout

import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing

final case class Container[+Msg](
    child: UIElement[Msg],
    padding: Spacing,
    margin: Spacing,
    backgroundColor: Option[RGBA],
    width: Option[String],
    height: Option[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Msg]:
  type T = Container[Nothing]

  def withPadding(padding: Spacing): Container[Msg] =
    this.copy(padding = padding)

  def withMargin(margin: Spacing): Container[Msg] =
    this.copy(margin = margin)

  def withBackgroundColor(color: RGBA): Container[Msg] =
    this.copy(backgroundColor = Some(color))

  def withWidth(width: String): Container[Msg] =
    this.copy(width = Some(width))

  def withHeight(height: String): Container[Msg] =
    this.copy(height = Some(height))

  def fillWidth: Container[Msg]  = withWidth("100%")
  def fillHeight: Container[Msg] = withHeight("100%")
  def fill: Container[Msg]       = fillWidth.fillHeight

  def modifyTheme(f: Theme => Theme): T =
    this.copy(_modifyTheme = Some(f)).asInstanceOf[T]

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Container.toHtml(this)

object Container:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.Style

  def apply[Msg](child: UIElement[Msg]): Container[Msg] =
    Container(
      child = child,
      padding = Spacing.None,
      margin = Spacing.None,
      backgroundColor = None,
      width = None,
      height = None,
      _modifyTheme = None
    )

  def toHtml[Msg](el: Container[Msg])(using theme: Theme): Html[Msg] =
    val t = el._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val stylesList = List(
      Some("padding" -> el.padding.toCSSValue),
      Some("margin"  -> el.margin.toCSSValue),
      el.backgroundColor.map(color => "background-color" -> color.toHexString("#")),
      el.width.map(w => "width" -> w),
      el.height.map(h => "height" -> h)
    ).flatten

    val baseStyles = Style(stylesList*)
    val childHtml  = el.child.toHtml(using t)

    div(style(baseStyles))(childHtml)
