package tyrian.ui.text

import tyrian.EmptyAttribute
import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.LineHeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.TextAlignment
import tyrian.ui.datatypes.TextDecoration
import tyrian.ui.datatypes.TextStyle

final case class TextBlock(
    value: String,
    variant: TextVariant,
    classNames: Set[String],
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[TextBlock, Nothing]:

  def withValue(value: String): TextBlock =
    this.copy(value = value)

  def toNormal: TextBlock   = this.copy(variant = TextVariant.Normal)
  def toHeading1: TextBlock = this.copy(variant = TextVariant.Heading1)
  def toHeading2: TextBlock = this.copy(variant = TextVariant.Heading2)
  def toHeading3: TextBlock = this.copy(variant = TextVariant.Heading3)
  def toHeading4: TextBlock = this.copy(variant = TextVariant.Heading4)
  def toHeading5: TextBlock = this.copy(variant = TextVariant.Heading5)
  def toHeading6: TextBlock = this.copy(variant = TextVariant.Heading6)
  def toCaption: TextBlock  = this.copy(variant = TextVariant.Caption)
  def toCode: TextBlock     = this.copy(variant = TextVariant.Code)
  def toLabel: TextBlock    = this.copy(variant = TextVariant.Label)

  def bold: TextBlock                               = modifyTextTheme(_.withWeight(FontWeight.Bold))
  def italic: TextBlock                             = modifyTextTheme(_.withStyle(TextStyle.Italic))
  def underlined: TextBlock                         = modifyTextTheme(_.withDecoration(TextDecoration.Underline))
  def strikethrough: TextBlock                      = modifyTextTheme(_.withDecoration(TextDecoration.Strikethrough))
  def withColor(color: RGBA): TextBlock             = modifyTextTheme(_.withColor(color))
  def withSize(size: FontSize): TextBlock           = modifyTextTheme(_.withFontSize(size))
  def withLineHeight(height: LineHeight): TextBlock = modifyTextTheme(_.withLineHeight(height))
  def wrap: TextBlock                               = modifyTextTheme(_.withWrap(true))
  def noWrap: TextBlock                             = modifyTextTheme(_.withWrap(false))

  def clearWeight: TextBlock     = modifyTextTheme(_.withWeight(FontWeight.Normal))
  def clearStyle: TextBlock      = modifyTextTheme(_.withStyle(TextStyle.Normal))
  def clearDecoration: TextBlock = modifyTextTheme(_.withDecoration(TextDecoration.None))

  def alignLeft: TextBlock    = modifyTextTheme(_.withAlignment(TextAlignment.Left))
  def alignCenter: TextBlock  = modifyTextTheme(_.withAlignment(TextAlignment.Center))
  def alignRight: TextBlock   = modifyTextTheme(_.withAlignment(TextAlignment.Right))
  def alignJustify: TextBlock = modifyTextTheme(_.withAlignment(TextAlignment.Justify))

  def withClassNames(classes: Set[String]): TextBlock =
    this.copy(classNames = classes)

  def modifyTheme(f: Theme => Theme): TextBlock =
    this.copy(_modifyTheme = Some(f))

  def modifyTextTheme(f: TextTheme => TextTheme): TextBlock =
    val g: Theme => Theme = theme =>
      val currentVariantTheme = getVariantTheme(variant, theme)
      val modifiedTheme       = f(currentVariantTheme)
      theme.copy(text = updateVariantTheme(theme.text, variant, modifiedTheme))
    this.copy(_modifyTheme = Some(g))

  private def updateVariantTheme(themes: TextThemes, variant: TextVariant, newTheme: TextTheme): TextThemes =
    variant match
      case TextVariant.Normal    => themes.copy(normal = newTheme)
      case TextVariant.Paragraph => themes.copy(paragraph = newTheme)
      case TextVariant.Heading1  => themes.copy(heading1 = newTheme)
      case TextVariant.Heading2  => themes.copy(heading2 = newTheme)
      case TextVariant.Heading3  => themes.copy(heading3 = newTheme)
      case TextVariant.Heading4  => themes.copy(heading4 = newTheme)
      case TextVariant.Heading5  => themes.copy(heading5 = newTheme)
      case TextVariant.Heading6  => themes.copy(heading6 = newTheme)
      case TextVariant.Caption   => themes.copy(caption = newTheme)
      case TextVariant.Code      => themes.copy(code = newTheme)
      case TextVariant.Label     => themes.copy(label = newTheme)

  private def getVariantTheme(variant: TextVariant, theme: Theme): TextTheme =
    variant match
      case TextVariant.Normal    => theme.text.normal
      case TextVariant.Paragraph => theme.text.paragraph
      case TextVariant.Heading1  => theme.text.heading1
      case TextVariant.Heading2  => theme.text.heading2
      case TextVariant.Heading3  => theme.text.heading3
      case TextVariant.Heading4  => theme.text.heading4
      case TextVariant.Heading5  => theme.text.heading5
      case TextVariant.Heading6  => theme.text.heading6
      case TextVariant.Caption   => theme.text.caption
      case TextVariant.Code      => theme.text.code
      case TextVariant.Label     => theme.text.label

  def toHtml: Theme ?=> tyrian.Html[Nothing] =
    TextBlock.toHtml(this)

object TextBlock:

  import tyrian.Html
  import tyrian.Html.*

  def apply(value: String): TextBlock =
    TextBlock(value, TextVariant.Normal, Set(), None)

  def body(value: String): TextBlock     = TextBlock(value, TextVariant.Normal, Set(), None)
  def heading1(value: String): TextBlock = TextBlock(value, TextVariant.Heading1, Set(), None)
  def heading2(value: String): TextBlock = TextBlock(value, TextVariant.Heading2, Set(), None)
  def heading3(value: String): TextBlock = TextBlock(value, TextVariant.Heading3, Set(), None)
  def heading4(value: String): TextBlock = TextBlock(value, TextVariant.Heading4, Set(), None)
  def heading5(value: String): TextBlock = TextBlock(value, TextVariant.Heading5, Set(), None)
  def heading6(value: String): TextBlock = TextBlock(value, TextVariant.Heading6, Set(), None)
  def caption(value: String): TextBlock  = TextBlock(value, TextVariant.Caption, Set(), None)
  def code(value: String): TextBlock     = TextBlock(value, TextVariant.Code, Set(), None)
  def label(value: String): TextBlock    = TextBlock(value, TextVariant.Label, Set(), None)

  def toHtml(element: TextBlock)(using theme: Theme): Html[Nothing] =
    val t = element._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val textTheme = getVariantTheme(element.variant, t)
    val styles    = textTheme.toStyles(t)

    val classAttribute =
      if element.classNames.isEmpty then EmptyAttribute
      else cls := element.classNames.mkString(" ")

    element.variant match
      case TextVariant.Normal    => span(style(styles), classAttribute)(element.value)
      case TextVariant.Paragraph => p(style(styles), classAttribute)(element.value)
      case TextVariant.Heading1  => h1(style(styles), classAttribute)(element.value)
      case TextVariant.Heading2  => h2(style(styles), classAttribute)(element.value)
      case TextVariant.Heading3  => h3(style(styles), classAttribute)(element.value)
      case TextVariant.Heading4  => h4(style(styles), classAttribute)(element.value)
      case TextVariant.Heading5  => h5(style(styles), classAttribute)(element.value)
      case TextVariant.Heading6  => h6(style(styles), classAttribute)(element.value)
      case TextVariant.Caption   => span(style(styles), classAttribute)(element.value)
      case TextVariant.Code      => tyrian.Html.code(style(styles), classAttribute)(element.value)
      case TextVariant.Label     => tyrian.Html.label(style(styles), classAttribute)(element.value)

  private def getVariantTheme(variant: TextVariant, theme: Theme): TextTheme =
    variant match
      case TextVariant.Normal    => theme.text.normal
      case TextVariant.Paragraph => theme.text.paragraph
      case TextVariant.Heading1  => theme.text.heading1
      case TextVariant.Heading2  => theme.text.heading2
      case TextVariant.Heading3  => theme.text.heading3
      case TextVariant.Heading4  => theme.text.heading4
      case TextVariant.Heading5  => theme.text.heading5
      case TextVariant.Heading6  => theme.text.heading6
      case TextVariant.Caption   => theme.text.caption
      case TextVariant.Code      => theme.text.code
      case TextVariant.Label     => theme.text.label
