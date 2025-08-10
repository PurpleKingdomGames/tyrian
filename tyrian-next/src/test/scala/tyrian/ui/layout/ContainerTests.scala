package tyrian.ui.layout

import tyrian.Style
import tyrian.ui.TextBlock
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.RGBA
import tyrian.ui.theme.Theme

class ContainerTests extends munit.FunSuite {

  given Theme =
    Theme.default

  test("Should be able to render a container") {
    val actual =
      Container(TextBlock("Hello")).toElem.toString

    val expected =
      """<div style="display:flex;flex:1;justify-content:flex-start;align-items:flex-start;padding:0;"><span style="font-family:Arial, sans-serif;font-size:1rem;font-weight:400;color:rgba(51, 51, 51, 255);text-align:left;line-height:1.5rem;white-space:normal;">Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - rounded") {
    val actual =
      Container(TextBlock("Hello")).withThemeOverride(_.rounded).toElem.toString

    val containerStyles =
      Style(
        "border"        -> "0 none #000000ff",
        "border-radius" -> "0.25rem"
      )

    val baseStyles =
      Style(
        "display"         -> "flex",
        "flex"            -> "1",
        "justify-content" -> "flex-start",
        "align-items"     -> "flex-start",
        "padding"         -> "0"
      )

    val combinedStyles = baseStyles |+| containerStyles

    val expected =
      s"""<div style="${combinedStyles.asString}"><span style="font-family:Arial, sans-serif;font-size:1rem;font-weight:400;color:rgba(51, 51, 51, 255);text-align:left;line-height:1.5rem;white-space:normal;">Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - border") {
    val actual =
      Container(TextBlock("Hello"))
        .withThemeOverride(
          _.withBorder(Border.solid(BorderWidth.Medium, RGBA.Purple))
        )
        .toElem
        .toString

    val containerStyles =
      Style(
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0"
      )

    val baseStyles =
      Style(
        "display"         -> "flex",
        "flex"            -> "1",
        "justify-content" -> "flex-start",
        "align-items"     -> "flex-start",
        "padding"         -> "0"
      )

    val combinedStyles = baseStyles |+| containerStyles

    val expected =
      s"""<div style="${combinedStyles.asString}"><span style="font-family:Arial, sans-serif;font-size:1rem;font-weight:400;color:rgba(51, 51, 51, 255);text-align:left;line-height:1.5rem;white-space:normal;">Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - rounded + border") {
    val actual =
      Container(TextBlock("Hello"))
        .withThemeOverride(
          _.rounded
            .solidBorder(BorderWidth.Medium, RGBA.Purple)
        )
        .toElem
        .toString

    val containerStyles =
      Style(
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0.25rem"
      )

    val baseStyles =
      Style(
        "display"         -> "flex",
        "flex"            -> "1",
        "justify-content" -> "flex-start",
        "align-items"     -> "flex-start",
        "padding"         -> "0"
      )

    val combinedStyles = baseStyles |+| containerStyles

    val expected =
      s"""<div style="${combinedStyles.asString}"><span style="font-family:Arial, sans-serif;font-size:1rem;font-weight:400;color:rgba(51, 51, 51, 255);text-align:left;line-height:1.5rem;white-space:normal;">Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - border + rounded (reversed)") {
    val actual =
      Container(TextBlock("Hello"))
        .withThemeOverride(
          _.solidBorder(BorderWidth.Medium, RGBA.Purple).rounded
        )
        .toElem
        .toString

    val containerStyles =
      Style(
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0.25rem"
      )

    val baseStyles =
      Style(
        "display"         -> "flex",
        "flex"            -> "1",
        "justify-content" -> "flex-start",
        "align-items"     -> "flex-start",
        "padding"         -> "0"
      )

    val combinedStyles = baseStyles |+| containerStyles

    val expected =
      s"""<div style="${combinedStyles.asString}"><span style="font-family:Arial, sans-serif;font-size:1rem;font-weight:400;color:rgba(51, 51, 51, 255);text-align:left;line-height:1.5rem;white-space:normal;">Hello</span></div>"""

    assertEquals(actual, expected)
  }

}
