package tyrian.ui.theme

import tyrian.Style
import tyrian.ui.Theme

final case class ButtonTheme(
    backgroundColor: String,
    textColor: String,
    borderRadius: String,
    border: String,
    fontSize: String,
    fontWeight: String,
    boxShadow: String,
    cursor: String,
    padding: String,
    userSelect: String

    // This stuff needs more thought.

    // hoverBackgroundColor: String = "darkblue",
    // hoverTextColor: String = "white",
    // activeBackgroundColor: String = "navy",
    // activeTextColor: String = "white",
    // focusOutline: String = "2px solid lightblue",
    // focusBoxShadow: String = "0 0 0 2px rgba(173, 216, 230, 0.5)",
    // disabledBackgroundColor: String = "lightgray",
    // disabledTextColor: String = "darkgray",
    // disabledCursor: String = "not-allowed",
):

  def toStyles(theme: Theme): Style =
    Style(
      "font-family"      -> theme.fonts.body,
      "background-color" -> backgroundColor,
      "color"            -> textColor,
      "border-radius"    -> borderRadius,
      "border"           -> border,
      "font-size"        -> fontSize,
      "font-weight"      -> fontWeight,
      "box-shadow"       -> boxShadow,
      "cursor"           -> cursor,
      "padding"          -> padding,
      "user-select"      -> userSelect
    )

object ButtonTheme:

  val default: ButtonTheme =
    ButtonTheme(
      backgroundColor = "blue",
      textColor = "white",
      borderRadius = "4px",
      border = "none",
      fontSize = "16px",
      fontWeight = "bold",
      boxShadow = "0 2px 4px rgba(0, 0, 0, 0.1)",
      cursor = "pointer",
      padding = "8px 16px",
      userSelect = "none"
    )
