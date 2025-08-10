package tyrian.ui.elements.stateless.text

import tyrian.Style
import tyrian.ui.TextBlock
import tyrian.ui.theme.Theme

class TextBlockTests extends munit.FunSuite {

  given Theme =
    Theme.default

  test("Should be able to render a TextBlock") {
    val actual =
      TextBlock("Hello").toElem.toString

    val styles =
      Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "400",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal"
      )

    val expected =
      s"""<span style="${styles.asString}">Hello</span>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the variant (h1)") {
    val actual =
      TextBlock("Hello").toHeading1.toElem.toString

    val styles =
      Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "2rem",
        "font-weight" -> "600",
        "color"       -> "rgba(26, 26, 26, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.2rem",
        "white-space" -> "normal"
      )

    val expected =
      s"""<h1 style="${styles.asString}">Hello</h1>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - bold") {
    val actual =
      TextBlock("Hello")
        .withThemeOverride(_.bold)
        .toElem
        .toString

    val styles =
      Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "700",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal"
      )

    val expected =
      s"""<span style="${styles.asString}">Hello</span>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - italic") {
    val actual =
      TextBlock("Hello").withThemeOverride(_.italic).toElem.toString

    val styles =
      Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "400",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal",
        "font-style"  -> "italic"
      )

    val expected =
      s"""<span style="${styles.asString}">Hello</span>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - bold + italic") {
    val actual =
      TextBlock("Hello").withThemeOverride(_.bold.italic).toElem.toString

    val styles =
      Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "700",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal",
        "font-style"  -> "italic"
      )

    val expected =
      s"""<span style="${styles.asString}">Hello</span>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - italic + bold (reversed)") {
    val actual =
      TextBlock("Hello").withThemeOverride(_.italic.bold).toElem.toString

    val styles =
      Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "700",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal",
        "font-style"  -> "italic"
      )

    val expected =
      s"""<span style="${styles.asString}">Hello</span>"""

    assertEquals(actual, expected)
  }

}
