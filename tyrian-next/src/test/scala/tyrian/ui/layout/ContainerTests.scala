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
      Container(TextBlock("Hello").noTheme).toElem.toString

    val expected =
      """<div style="display:flex;flex:1;justify-content:flex-start;align-items:flex-start;padding:0;"><span>Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - rounded") {
    val actual =
      Container(TextBlock("Hello").noTheme)
        .overrideTheme(_.rounded)
        .toElem
        .toString

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
      s"""<div style="${combinedStyles.asString}"><span>Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - border") {
    val actual =
      Container(TextBlock("Hello").noTheme)
        .overrideTheme(
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
      s"""<div style="${combinedStyles.asString}"><span>Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - rounded + border") {
    val actual =
      Container(TextBlock("Hello").noTheme)
        .overrideTheme(
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
      s"""<div style="${combinedStyles.asString}"><span>Hello</span></div>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - border + rounded (reversed)") {
    val actual =
      Container(TextBlock("Hello").noTheme)
        .overrideTheme(
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
      s"""<div style="${combinedStyles.asString}"><span>Hello</span></div>"""

    assertEquals(actual, expected)
  }

}
