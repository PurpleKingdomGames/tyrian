package tyrian.ui.image

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.Extent
import tyrian.ui.datatypes.ImageFit
import tyrian.ui.datatypes.RGBA

final case class Image[+Msg](
    src: String,
    alt: String,
    width: Option[Extent],
    height: Option[Extent],
    fit: ImageFit,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Image[?], Msg]:

  def withSrc(src: String): Image[Msg] =
    this.copy(src = src)

  def withAlt(alt: String): Image[Msg] =
    this.copy(alt = alt)

  def withWidth(width: Extent): Image[Msg] =
    this.copy(width = Some(width))
  def fillWidth: Image[Msg] = withWidth(Extent.Fill)

  def withHeight(height: Extent): Image[Msg] =
    this.copy(height = Some(height))
  def fillHeight: Image[Msg] = withHeight(Extent.Fill)

  def withSize(width: Extent, height: Extent): Image[Msg] =
    this.copy(width = Some(width), height = Some(height))
  def fillContainer: Image[Msg] = withSize(Extent.Fill, Extent.Fill)

  def withFit(fit: ImageFit): Image[Msg] =
    this.copy(fit = fit)
  def cover: Image[Msg]     = withFit(ImageFit.Cover)
  def contain: Image[Msg]   = withFit(ImageFit.Contain)
  def fill: Image[Msg]      = withFit(ImageFit.Fill)
  def scaleDown: Image[Msg] = withFit(ImageFit.ScaleDown)

  def withBorder(border: Border): Image[Msg] =
    modifyImageTheme(_.withBorder(border))
  def modifyBorder(f: Border => Border): Image[Msg] =
    modifyImageTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Image[Msg] =
    modifyImageTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Image[Msg] =
    modifyImageTheme(_.dashedBorder(width, color))

  def square: Image[Msg]       = modifyImageTheme(_.square)
  def rounded: Image[Msg]      = modifyImageTheme(_.rounded)
  def roundedSmall: Image[Msg] = modifyImageTheme(_.roundedSmall)
  def roundedLarge: Image[Msg] = modifyImageTheme(_.roundedLarge)
  def circular: Image[Msg]     = modifyImageTheme(_.circular)

  def withBoxShadow(boxShadow: BoxShadow): Image[Msg] =
    modifyImageTheme(_.withBoxShadow(boxShadow))
  def noBoxShadow: Image[Msg] =
    modifyImageTheme(_.noBoxShadow)
  def modifyBoxShadow(f: BoxShadow => BoxShadow): Image[Msg] =
    modifyImageTheme(_.modifyBoxShadow(f))
  def shadowSmall(color: RGBA): Image[Msg] =
    modifyImageTheme(_.shadowSmall(color))
  def shadowMedium(color: RGBA): Image[Msg] =
    modifyImageTheme(_.shadowMedium(color))
  def shadowLarge(color: RGBA): Image[Msg] =
    modifyImageTheme(_.shadowLarge(color))
  def shadowExtraLarge(color: RGBA): Image[Msg] =
    modifyImageTheme(_.shadowExtraLarge(color))

  def withClassNames(classes: Set[String]): Image[Msg] =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Image[Msg] =
    val h =
      _modifyTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(_modifyTheme = Some(h))

  def modifyImageTheme(f: ImageTheme => ImageTheme): Image[Msg] =
    val g: Theme => Theme = theme => theme.copy(image = f(theme.image))
    modifyTheme(g)

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

  def apply[Msg](src: String, alt: String, width: Extent, height: Extent): Image[Msg] =
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
      image.width.map(w => width := w.toCSSValue).toList,
      image.height.map(h => height := h.toCSSValue).toList
    ).flatten

    val styles =
      image.fit.toStyle |+| t.image.toStyle

    val classAttribute =
      if image.classNames.isEmpty then EmptyAttribute
      else cls := image.classNames.mkString(" ")

    val allAttributes = baseAttributes ++ sizeAttributes ++ List(style(styles), classAttribute)

    img(allAttributes*)
