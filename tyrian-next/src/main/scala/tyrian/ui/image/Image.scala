package tyrian.ui.image

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.Extent
import tyrian.ui.datatypes.ImageFit
import tyrian.ui.datatypes.Opacity
import tyrian.ui.datatypes.RGBA
import tyrian.ui.layout.ContainerTheme

final case class Image(
    src: String,
    alt: String,
    width: Option[Extent],
    height: Option[Extent],
    fit: ImageFit,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Image, Nothing]:

  def withSrc(src: String): Image =
    this.copy(src = src)

  def withAlt(alt: String): Image =
    this.copy(alt = alt)

  def withWidth(width: Extent): Image =
    this.copy(width = Some(width))
  def fillWidth: Image = withWidth(Extent.Fill)

  def withHeight(height: Extent): Image =
    this.copy(height = Some(height))
  def fillHeight: Image = withHeight(Extent.Fill)

  def withSize(width: Extent, height: Extent): Image =
    this.copy(width = Some(width), height = Some(height))
  def fillContainer: Image = withSize(Extent.Fill, Extent.Fill)

  def withFit(fit: ImageFit): Image =
    this.copy(fit = fit)
  def cover: Image     = withFit(ImageFit.Cover)
  def contain: Image   = withFit(ImageFit.Contain)
  def fill: Image      = withFit(ImageFit.Fill)
  def scaleDown: Image = withFit(ImageFit.ScaleDown)

  def withBorder(border: Border): Image =
    modifyImageTheme(_.withBorder(border))
  def modifyBorder(f: Border => Border): Image =
    modifyImageTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Image =
    modifyImageTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Image =
    modifyImageTheme(_.dashedBorder(width, color))

  def square: Image       = modifyImageTheme(_.square)
  def rounded: Image      = modifyImageTheme(_.rounded)
  def roundedSmall: Image = modifyImageTheme(_.roundedSmall)
  def roundedLarge: Image = modifyImageTheme(_.roundedLarge)
  def circular: Image     = modifyImageTheme(_.circular)

  def withBoxShadow(boxShadow: BoxShadow): Image =
    modifyImageTheme(_.withBoxShadow(boxShadow))
  def noBoxShadow: Image =
    modifyImageTheme(_.noBoxShadow)
  def modifyBoxShadow(f: BoxShadow => BoxShadow): Image =
    modifyImageTheme(_.modifyBoxShadow(f))
  def shadowSmall(color: RGBA): Image =
    modifyImageTheme(_.shadowSmall(color))
  def shadowMedium(color: RGBA): Image =
    modifyImageTheme(_.shadowMedium(color))
  def shadowLarge(color: RGBA): Image =
    modifyImageTheme(_.shadowLarge(color))
  def shadowExtraLarge(color: RGBA): Image =
    modifyImageTheme(_.shadowExtraLarge(color))

  def withOpacity(opacity: Opacity): Image =
    modifyImageTheme(_.withOpacity(opacity))
  def noOpacity: Image =
    modifyImageTheme(_.noOpacity)
  def fullyOpaque: Image =
    modifyImageTheme(_.fullyOpaque)
  def semiTransparent: Image =
    modifyImageTheme(_.semiTransparent)
  def transparent: Image =
    modifyImageTheme(_.transparent)

  def withBackgroundColor(color: RGBA): Image =
    modifyImageTheme(_.withBackgroundColor(color))
  def noBackgroundColor: Image =
    modifyImageTheme(_.noBackgroundColor)

  def withClassNames(classes: Set[String]): Image =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): Image =
    val h =
      _modifyTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(_modifyTheme = Some(h))

  def modifyImageTheme(f: ContainerTheme => ContainerTheme): Image =
    val g: Theme => Theme = theme => theme.copy(image = f(theme.image))
    modifyTheme(g)

  def toHtml: Theme ?=> tyrian.Html[Nothing] =
    Image.toHtml(this)

object Image:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.Style

  def apply(src: String): Image =
    Image(
      src = src,
      alt = "",
      width = None,
      height = None,
      fit = ImageFit.default,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def apply(src: String, alt: String): Image =
    Image(
      src = src,
      alt = alt,
      width = None,
      height = None,
      fit = ImageFit.default,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def apply(src: String, alt: String, width: Extent, height: Extent): Image =
    Image(
      src = src,
      alt = alt,
      width = Some(width),
      height = Some(height),
      fit = ImageFit.default,
      classNames = Set.empty,
      _modifyTheme = None
    )

  def toHtml[Msg](image: Image)(using theme: Theme): Html[Msg] =
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
