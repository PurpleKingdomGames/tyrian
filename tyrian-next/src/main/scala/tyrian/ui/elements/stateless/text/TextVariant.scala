package tyrian.ui.elements.stateless.text

import tyrian.Style
import tyrian.next.GlobalMsg
import tyrian.ui.Theme

/** Semantic text variants that determine both styling and HTML element output */
enum TextVariant derives CanEqual:
  case Normal
  case Paragraph
  case Heading1
  case Heading2
  case Heading3
  case Heading4
  case Heading5
  case Heading6
  case Caption
  case Code
  case Label

  def giveTextThemeVariant(textTheme: TextThemes): TextTheme =
    this match
      case TextVariant.Normal    => textTheme.normal
      case TextVariant.Paragraph => textTheme.paragraph
      case TextVariant.Heading1  => textTheme.heading1
      case TextVariant.Heading2  => textTheme.heading2
      case TextVariant.Heading3  => textTheme.heading3
      case TextVariant.Heading4  => textTheme.heading4
      case TextVariant.Heading5  => textTheme.heading5
      case TextVariant.Heading6  => textTheme.heading6
      case TextVariant.Caption   => textTheme.caption
      case TextVariant.Code      => textTheme.code
      case TextVariant.Label     => textTheme.label

  def giveThemeVariant(theme: Theme): Option[TextTheme] =
    theme match
      case Theme.NoStyles =>
        None

      case t: Theme.Styles =>
        Some(giveTextThemeVariant(t.text))

  def toHtml(element: TextBlock)(using theme: Theme): tyrian.Html[GlobalMsg] =
    TextVariant.toHtml(element)

object TextVariant:

  import tyrian.Html
  import tyrian.Html.*
  import tyrian.EmptyAttribute

  def toHtml(element: TextBlock)(using theme: Theme): Html[GlobalMsg] =
    val textTheme = element.variant.giveThemeVariant(theme)
    val styles    = textTheme.map(_.toStyles(theme)).getOrElse(Style.empty)

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
