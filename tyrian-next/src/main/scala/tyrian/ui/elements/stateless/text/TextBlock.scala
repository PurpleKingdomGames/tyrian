package tyrian.ui.elements.stateless.text

import tyrian.next.GlobalMsg
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class TextBlock(
    value: String,
    variant: TextVariant,
    classNames: Set[String],
    themeOverride: ThemeOverride[TextTheme]
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

  def themeLens: Lens[Theme.Default, TextTheme] =
    Lens(
      _.text.getFromVariant(variant),
      (t, txt) => t.copy(text = t.text.setFromVariant(variant, txt))
    )

  def withThemeOverride(value: ThemeOverride[TextTheme]): TextBlock =
    this.copy(themeOverride = value)

  def view: Theme ?=> tyrian.Html[GlobalMsg] =
    TextBlock.toHtml(this)

object TextBlock:

  def apply(value: String): TextBlock =
    TextBlock(value, TextVariant.Normal, Set(), ThemeOverride.NoOverride)

  def apply(value: String, variant: TextVariant): TextBlock =
    TextBlock(value, variant, Set(), ThemeOverride.NoOverride)

  def body(value: String): TextBlock     = TextBlock(value, TextVariant.Normal)
  def heading1(value: String): TextBlock = TextBlock(value, TextVariant.Heading1)
  def heading2(value: String): TextBlock = TextBlock(value, TextVariant.Heading2)
  def heading3(value: String): TextBlock = TextBlock(value, TextVariant.Heading3)
  def heading4(value: String): TextBlock = TextBlock(value, TextVariant.Heading4)
  def heading5(value: String): TextBlock = TextBlock(value, TextVariant.Heading5)
  def heading6(value: String): TextBlock = TextBlock(value, TextVariant.Heading6)
  def caption(value: String): TextBlock  = TextBlock(value, TextVariant.Caption)
  def code(value: String): TextBlock     = TextBlock(value, TextVariant.Code)
  def label(value: String): TextBlock    = TextBlock(value, TextVariant.Label)

  def toHtml(element: TextBlock)(using theme: Theme): tyrian.Html[GlobalMsg] =
    element.variant.toHtml(element)
