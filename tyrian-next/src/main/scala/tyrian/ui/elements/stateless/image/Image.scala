package tyrian.ui.elements.stateless.image

import tyrian.EmptyAttribute
import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.datatypes.Extent
import tyrian.ui.datatypes.ImageFit
import tyrian.ui.layout.ContainerTheme
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Image(
    src: String,
    alt: String,
    width: Option[Extent],
    height: Option[Extent],
    fit: ImageFit,
    classNames: Set[String],
    themeOverride: ThemeOverride[ContainerTheme]
) extends UIElement[Image, ContainerTheme]:

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

  def withClassNames(classes: Set[String]): Image =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme.Default, ContainerTheme] =
    Lens(
      _.image,
      (t, i) => t.copy(image = i)
    )

  def withThemeOverride(value: ThemeOverride[ContainerTheme]): Image =
    this.copy(themeOverride = value)

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
      themeOverride = ThemeOverride.NoOverride
    )

  def apply(src: String, alt: String): Image =
    Image(
      src = src,
      alt = alt,
      width = None,
      height = None,
      fit = ImageFit.default,
      classNames = Set.empty,
      themeOverride = ThemeOverride.NoOverride
    )

  def apply(src: String, alt: String, width: Extent, height: Extent): Image =
    Image(
      src = src,
      alt = alt,
      width = Some(width),
      height = Some(height),
      fit = ImageFit.default,
      classNames = Set.empty,
      themeOverride = ThemeOverride.NoOverride
    )

  def toHtml(image: Image)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val baseAttributes = List(
      src := image.src,
      alt := image.alt
    )

    val sizeAttributes = List(
      image.width.map(w => width := w.toCSSValue).toList,
      image.height.map(h => height := h.toCSSValue).toList
    ).flatten

    val imageStyles =
      theme match
        case Theme.None =>
          Style.empty

        case tt: Theme.Default =>
          tt.image.toStyle

    val styles =
      image.fit.toStyle |+| imageStyles

    val classAttribute =
      if image.classNames.isEmpty then EmptyAttribute
      else cls := image.classNames.mkString(" ")

    val allAttributes = baseAttributes ++ sizeAttributes ++ List(style(styles), classAttribute)

    img(allAttributes*)
