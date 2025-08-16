package tyrian.ui.elements.stateful.input

import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme

final case class InputTheme(
    fontSize: FontSize,
    fontWeight: FontWeight,
    textColor: RGBA,
    backgroundColor: RGBA,
    border: Option[Border],
    padding: Spacing,
    disabledBackgroundColor: RGBA,
    disabledTextColor: RGBA,
    disabledBorderColor: RGBA
):

  def withFontSize(value: FontSize): InputTheme =
    this.copy(fontSize = value)

  def withFontWeight(value: FontWeight): InputTheme =
    this.copy(fontWeight = value)

  def withTextColor(value: RGBA): InputTheme =
    this.copy(textColor = value)

  def withBackgroundColor(value: RGBA): InputTheme =
    this.copy(backgroundColor = value)

  def withBorder(border: Border): InputTheme =
    this.copy(border = Some(border))

  def noBorder: InputTheme =
    this.copy(border = None)

  def modifyBorder(f: Border => Border): InputTheme =
    withBorder(
      border match
        case Some(b) => f(b)
        case None    => f(Border.default)
    )

  def solidBorder(width: BorderWidth, color: RGBA): InputTheme =
    modifyBorder(_.withStyle(BorderStyle.Solid).withWidth(width).withColor(color))
  def dashedBorder(width: BorderWidth, color: RGBA): InputTheme =
    modifyBorder(_.withStyle(BorderStyle.Dashed).withWidth(width).withColor(color))

  def square: InputTheme       = withBorderRadius(BorderRadius.None)
  def rounded: InputTheme      = withBorderRadius(BorderRadius.Medium)
  def roundedSmall: InputTheme = withBorderRadius(BorderRadius.Small)
  def roundedLarge: InputTheme = withBorderRadius(BorderRadius.Large)
  def circular: InputTheme     = withBorderRadius(BorderRadius.Full)

  def withBorderRadius(radius: BorderRadius): InputTheme =
    modifyBorder(_.withRadius(radius))

  def withBorderColor(value: RGBA): InputTheme =
    modifyBorder(_.withColor(value))

  def withBorderWidth(value: BorderWidth): InputTheme =
    modifyBorder(_.withWidth(value))

  def withBorderStyle(value: BorderStyle): InputTheme =
    modifyBorder(_.withStyle(value))

  def withPadding(value: Spacing): InputTheme =
    this.copy(padding = value)

  def withDisabledBackgroundColor(value: RGBA): InputTheme =
    this.copy(disabledBackgroundColor = value)

  def withDisabledTextColor(value: RGBA): InputTheme =
    this.copy(disabledTextColor = value)

  def withDisabledBorderColor(value: RGBA): InputTheme =
    this.copy(disabledBorderColor = value)

  def toStyles(theme: Theme): Style =
    theme match
      case Theme.None =>
        Style.empty

      case t: Theme.Default =>
        val borderStyle = border.map(_.toStyle).getOrElse(Style.empty)

        Style(
          "font-family"      -> t.fonts.body.toCSSValue,
          "font-size"        -> fontSize.toCSSValue,
          "font-weight"      -> fontWeight.toCSSValue,
          "color"            -> textColor.toCSSValue,
          "background-color" -> backgroundColor.toCSSValue,
          "padding"          -> padding.toCSSValue,
          "box-sizing"       -> "border-box",
          "outline"          -> "none"
        ) |+| borderStyle

  def toDisabledStyles(theme: Theme): Style =
    toStyles(theme) |+| Style(
      "color"            -> disabledTextColor.toCSSValue,
      "background-color" -> disabledBackgroundColor.toCSSValue,
      "border-color"     -> disabledBorderColor.toCSSValue,
      "cursor"           -> "not-allowed"
    )

object InputTheme:

  val default: InputTheme =
    InputTheme(
      fontSize = FontSize.Small,
      fontWeight = FontWeight.Normal,
      textColor = RGBA.fromHex("#374151"),
      backgroundColor = RGBA.White,
      border = None,
      padding = Spacing.px(8),
      disabledBackgroundColor = RGBA.fromHex("#f9fafb"),
      disabledTextColor = RGBA.fromHex("#9ca3af"),
      disabledBorderColor = RGBA.fromHex("#e5e7eb")
    )
