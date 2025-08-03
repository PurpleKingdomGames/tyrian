package tyrian.ui.text

import tyrian.ui.Theme
import tyrian.ui.UIElement
import tyrian.ui.datatypes.RGBA

final case class Text(
    value: String,
    variant: TextVariant,
    _modifyTheme: Option[Theme => Theme]
) extends UIElement[Nothing]:
  type T = Text

  def withValue(value: String): Text =
    this.copy(value = value)

  def toNormal: Text   = this.copy(variant = TextVariant.Normal)
  def toHeading1: Text = this.copy(variant = TextVariant.Heading1)
  def toHeading2: Text = this.copy(variant = TextVariant.Heading2)
  def toHeading3: Text = this.copy(variant = TextVariant.Heading3)
  def toHeading4: Text = this.copy(variant = TextVariant.Heading4)
  def toHeading5: Text = this.copy(variant = TextVariant.Heading5)
  def toHeading6: Text = this.copy(variant = TextVariant.Heading6)
  def toCaption: Text  = this.copy(variant = TextVariant.Caption)
  def toCode: Text     = this.copy(variant = TextVariant.Code)
  def toLabel: Text    = this.copy(variant = TextVariant.Label)

  // TODO: Add clearBold - or maybe just 'normal'?
  def bold: Text = modifyTextTheme(_.copy(fontWeight = "bold"))
  // TODO: Add clearItalic - or maybe just 'normal'?
  def italic: Text = modifyTextTheme(_.copy(fontStyle = Some("italic")))
  // TODO: Add clearUnderlined - or maybe just 'normal'?
  def underlined: Text = modifyTextTheme(_.copy(textDecoration = Some("underline")))
  // TODO: Strikethrough?
  def withColor(color: RGBA): Text = modifyTextTheme(_.copy(color = color))
  def withSize(size: String): Text = modifyTextTheme(_.copy(fontSize = size))
  def wrap: Text                   = modifyTextTheme(_.copy(wrap = true))
  def noWrap: Text                 = modifyTextTheme(_.copy(wrap = false))

  def alignLeft: Text   = modifyTextTheme(_.copy(textAlign = "left"))
  def alignCenter: Text = modifyTextTheme(_.copy(textAlign = "center"))
  def alignRight: Text  = modifyTextTheme(_.copy(textAlign = "right"))

  def modifyTheme(f: Theme => Theme): Text =
    this.copy(_modifyTheme = Some(f))

  def modifyTextTheme(f: TextTheme => TextTheme): Text =
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
    Text.toHtml(this)

object Text:

  import tyrian.Html
  import tyrian.Html.*

  def apply(value: String): Text =
    Text(value, TextVariant.Normal, None)

  def body(value: String): Text     = Text(value, TextVariant.Normal, None)
  def heading1(value: String): Text = Text(value, TextVariant.Heading1, None)
  def heading2(value: String): Text = Text(value, TextVariant.Heading2, None)
  def heading3(value: String): Text = Text(value, TextVariant.Heading3, None)
  def heading4(value: String): Text = Text(value, TextVariant.Heading4, None)
  def heading5(value: String): Text = Text(value, TextVariant.Heading5, None)
  def heading6(value: String): Text = Text(value, TextVariant.Heading6, None)
  def caption(value: String): Text  = Text(value, TextVariant.Caption, None)
  def code(value: String): Text     = Text(value, TextVariant.Code, None)
  def label(value: String): Text    = Text(value, TextVariant.Label, None)

  def toHtml(element: Text)(using theme: Theme): Html[Nothing] =
    val t = element._modifyTheme match
      case Some(f) => f(theme)
      case None    => theme

    val textTheme = getVariantTheme(element.variant, t)
    val styles    = textTheme.toStyles(t)

    element.variant match
      case TextVariant.Normal    => span(style(styles))(element.value)
      case TextVariant.Paragraph => p(style(styles))(element.value)
      case TextVariant.Heading1  => h1(style(styles))(element.value)
      case TextVariant.Heading2  => h2(style(styles))(element.value)
      case TextVariant.Heading3  => h3(style(styles))(element.value)
      case TextVariant.Heading4  => h4(style(styles))(element.value)
      case TextVariant.Heading5  => h5(style(styles))(element.value)
      case TextVariant.Heading6  => h6(style(styles))(element.value)
      case TextVariant.Caption   => span(style(styles))(element.value)
      case TextVariant.Code      => tyrian.Html.code(style(styles))(element.value)
      case TextVariant.Label     => tyrian.Html.label(style(styles))(element.value)

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
