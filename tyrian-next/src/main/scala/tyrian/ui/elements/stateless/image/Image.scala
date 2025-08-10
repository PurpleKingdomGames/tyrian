package tyrian.ui.elements.stateless.image

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.BackgroundMode
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.BoxShadow
import tyrian.ui.datatypes.Extent
import tyrian.ui.datatypes.Fill
import tyrian.ui.datatypes.ImageFit
import tyrian.ui.datatypes.Opacity
import tyrian.ui.datatypes.Position
import tyrian.ui.datatypes.RGBA
import tyrian.ui.layout.ContainerTheme
import tyrian.ui.theme.Theme

final case class Image(
    src: String,
    alt: String,
    width: Option[Extent],
    height: Option[Extent],
    fit: ImageFit,
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
) extends UIElement[Image]:

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
    overrideImageTheme(_.withBorder(border))
  def modifyBorder(f: Border => Border): Image =
    overrideImageTheme(_.modifyBorder(f))
  def solidBorder(width: BorderWidth, color: RGBA): Image =
    overrideImageTheme(_.solidBorder(width, color))
  def dashedBorder(width: BorderWidth, color: RGBA): Image =
    overrideImageTheme(_.dashedBorder(width, color))

  def square: Image       = overrideImageTheme(_.square)
  def rounded: Image      = overrideImageTheme(_.rounded)
  def roundedSmall: Image = overrideImageTheme(_.roundedSmall)
  def roundedLarge: Image = overrideImageTheme(_.roundedLarge)
  def circular: Image     = overrideImageTheme(_.circular)

  def withBoxShadow(boxShadow: BoxShadow): Image =
    overrideImageTheme(_.withBoxShadow(boxShadow))
  def noBoxShadow: Image =
    overrideImageTheme(_.noBoxShadow)
  def modifyBoxShadow(f: BoxShadow => BoxShadow): Image =
    overrideImageTheme(_.modifyBoxShadow(f))
  def shadowSmall(color: RGBA): Image =
    overrideImageTheme(_.shadowSmall(color))
  def shadowMedium(color: RGBA): Image =
    overrideImageTheme(_.shadowMedium(color))
  def shadowLarge(color: RGBA): Image =
    overrideImageTheme(_.shadowLarge(color))
  def shadowExtraLarge(color: RGBA): Image =
    overrideImageTheme(_.shadowExtraLarge(color))

  def withOpacity(opacity: Opacity): Image =
    overrideImageTheme(_.withOpacity(opacity))
  def noOpacity: Image =
    overrideImageTheme(_.noOpacity)
  def fullyOpaque: Image =
    overrideImageTheme(_.fullyOpaque)
  def semiTransparent: Image =
    overrideImageTheme(_.semiTransparent)
  def transparent: Image =
    overrideImageTheme(_.transparent)

  def withBackgroundColor(color: RGBA): Image =
    overrideImageTheme(_.withBackgroundColor(color))
  def noBackground: Image =
    overrideImageTheme(_.noBackground)

  def withBackgroundFill(fill: Fill): Image =
    overrideImageTheme(_.withBackgroundFill(fill))

  def withBackgroundImage(url: String): Image =
    withBackgroundFill(Fill.Image(url))
  def withBackgroundImageAt(url: String, position: Position): Image =
    withBackgroundFill(Fill.Image(url).withPosition(position))
  def withBackgroundImageCover(url: String): Image =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.coverNoRepeat))
  def withBackgroundImageContain(url: String): Image =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.containNoRepeat))
  def withBackgroundImageFill(url: String): Image =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.fillNoRepeat))
  def withBackgroundImageTiled(url: String): Image =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.autoRepeat))
  def withBackgroundImageRepeatX(url: String): Image =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.autoRepeatX))
  def withBackgroundImageRepeatY(url: String): Image =
    withBackgroundFill(Fill.Image(url).withMode(BackgroundMode.autoRepeatY))

  def withClassNames(classes: Set[String]): Image =
    this.copy(classNames = classes)

  def withThemeOverride(f: Theme => Theme): Image =
    val h =
      overrideLocalTheme match
        case Some(g) => f andThen g
        case None    => f

    this.copy(overrideLocalTheme = Some(h))

  def overrideImageTheme(f: ContainerTheme => ContainerTheme): Image =
    val g: Theme => Theme = theme => theme.copy(image = f(theme.image))
    withThemeOverride(g)

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Image.toHtml(this)

object Image:

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
      overrideLocalTheme = None
    )

  def apply(src: String, alt: String): Image =
    Image(
      src = src,
      alt = alt,
      width = None,
      height = None,
      fit = ImageFit.default,
      classNames = Set.empty,
      overrideLocalTheme = None
    )

  def apply(src: String, alt: String, width: Extent, height: Extent): Image =
    Image(
      src = src,
      alt = alt,
      width = Some(width),
      height = Some(height),
      fit = ImageFit.default,
      classNames = Set.empty,
      overrideLocalTheme = None
    )

  def toHtml(image: Image)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val t = image.overrideLocalTheme match
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
