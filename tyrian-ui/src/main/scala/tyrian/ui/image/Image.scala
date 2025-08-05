package tyrian.ui.image

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.ImageFit
import tyrian.ui.datatypes.RGBA

final case class Image[+Msg](
    src: String,
    alt: String,
    width: Option[String],  // TODO: Yuk. Strings for sizes!
    height: Option[String], // TODO: Yuk. Strings for sizes!
    fit: ImageFit,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Image[?], Msg]:

  def withSrc(src: String): Image[Msg] =
    this.copy(src = src)

  def withAlt(alt: String): Image[Msg] =
    this.copy(alt = alt)

  def withWidth(width: String): Image[Msg] =
    this.copy(width = Some(width))
  def fillWidth: Image[Msg] = withWidth("100%")

  def withHeight(height: String): Image[Msg] =
    this.copy(height = Some(height))
  def fillHeight: Image[Msg] = withHeight("100%")

  def withSize(width: String, height: String): Image[Msg] =
    this.copy(width = Some(width), height = Some(height))
  def fillContainer: Image[Msg] = withSize("100%", "100%")

  def withFit(fit: ImageFit): Image[Msg] =
    this.copy(fit = fit)
  def cover: Image[Msg]     = withFit(ImageFit.Cover)
  def contain: Image[Msg]   = withFit(ImageFit.Contain)
  def fill: Image[Msg]      = withFit(ImageFit.Fill)
  def scaleDown: Image[Msg] = withFit(ImageFit.ScaleDown)

  def withSolidBorder(width: BorderWidth, color: RGBA): Image[Msg] =
    modifyImageTheme(_.withBorder(Border.solid(width, color)))
  def withDashedBorder(width: BorderWidth, color: RGBA): Image[Msg] =
    modifyImageTheme(_.withBorder(Border.dashed(width, color)))

  def rounded: Image[Msg]      = modifyImageTheme(_.withBorderRadius(BorderRadius.Medium))
  def roundedSmall: Image[Msg] = modifyImageTheme(_.withBorderRadius(BorderRadius.Small))
  def roundedLarge: Image[Msg] = modifyImageTheme(_.withBorderRadius(BorderRadius.Large))
  def circular: Image[Msg]     = modifyImageTheme(_.withBorderRadius(BorderRadius.Full))

  def withClassNames(classes: Set[String]): Image[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Image[Msg] =
    this.copy(_modifyTheme = Some(f))

  def modifyImageTheme(f: ImageTheme => ImageTheme): Image[Msg] =
    val g: Theme => Theme = theme => theme.copy(image = f(theme.image))

    _modifyTheme match
      case Some(f) =>
        this.copy(_modifyTheme = Some(f andThen g))

      case None =>
        this.copy(_modifyTheme = Some(g))

  def toHtml: Theme ?=> tyrian.Html[Msg] =
    Image.toHtml(this)

object Image:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.Style

  def apply[Msg](src: String): Image[Msg] =
    Image(
      src = src,
      alt = "",
      width = None,
      height = None,
      fit = ImageFit.default,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def apply[Msg](src: String, alt: String): Image[Msg] =
    Image(
      src = src,
      alt = alt,
      width = None,
      height = None,
      fit = ImageFit.default,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def apply[Msg](src: String, alt: String, width: String, height: String): Image[Msg] =
    Image(
      src = src,
      alt = alt,
      width = Some(width),
      height = Some(height),
      fit = ImageFit.default,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def toHtml[Msg](image: Image[Msg])(using theme: Theme): Html[Msg] =
    val t = image._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val baseAttributes = List(
      src := image.src,
      alt := image.alt
    )

    val sizeAttributes = List(
      image.width.map(w => width := w).toList,
      image.height.map(h => height := h).toList
    ).flatten

    val styles =
      image.fit.toStyle |+| t.image.toStyle

    val classAttribute =
      if image.classNames.isEmpty then EmptyAttribute
      else cls := image.classNames.mkString(" ")

    val allAttributes = baseAttributes ++ sizeAttributes ++ List(style(styles), classAttribute)

    img(allAttributes*)
