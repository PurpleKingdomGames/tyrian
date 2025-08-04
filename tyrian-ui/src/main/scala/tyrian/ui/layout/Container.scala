package tyrian.ui.layout

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing

final case class Container[+Msg](
    child: UIElement[?, Msg],
    padding: Spacing,
    margin: Spacing,
    backgroundColor: Option[RGBA],
    width: Option[String],
    height: Option[String],
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Container[?], Msg]:

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

  def withClassNames(classes: Set[String]): Container[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Container[Msg] =
    this.copy(_modifyTheme = Some(f))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Container.toHtml(this)

object Container:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.Style

  def apply[Msg](child: UIElement[?, Msg]): Container[Msg] =
    Container(
      child = child,
      padding = Spacing.None,
      margin = Spacing.None,
      backgroundColor = None,
      width = None,
      height = None,
      classNames = Set(),
      _modifyTheme = None
    )

  def toHtml[Msg](container: Container[Msg])(using theme: Theme): Html[Msg] =
    val t = container._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val stylesList = List(
      Some("padding" -> container.padding.toCSSValue),
      Some("margin"  -> container.margin.toCSSValue),
      container.backgroundColor.map(color => "background-color" -> color.toHexString("#")),
      container.width.map(w => "width" -> w),
      container.height.map(h => "height" -> h)
    ).flatten

    val classAttribute =
      if container.classNames.isEmpty then EmptyAttribute
      else cls := container.classNames.mkString(" ")

    val baseStyles = Style(stylesList*)
    val childHtml  = container.child.toHtml(using t)

    div(style(baseStyles), classAttribute)(childHtml)
