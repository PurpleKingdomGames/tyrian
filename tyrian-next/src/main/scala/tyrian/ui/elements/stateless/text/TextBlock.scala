package tyrian.ui.elements.stateless.text

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.utils.Lens

final case class TextBlock(
    value: String,
    variant: TextVariant,
    classNames: Set[String],
    themeOverride: Option[TextTheme => TextTheme]
) extends UIElement[TextBlock, TextTheme]:

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

  def withClassNames(classes: Set[String]): TextBlock =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme, TextTheme] =
    Lens(
      _.text.getFromVariant(variant),
      (t, txt) => t.copy(text = t.text.setFromVariant(variant, txt))
    )

  def withThemeOverride(f: TextTheme => TextTheme): TextBlock =
    this.copy(themeOverride = Some(f))

  def view: Theme ?=> tyrian.Html[GlobalMsg] =
    TextBlock.toHtml(this)

object TextBlock:

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

  def toHtml(element: TextBlock)(using theme: Theme): tyrian.Html[GlobalMsg] =
    element.variant.toHtml(element)
