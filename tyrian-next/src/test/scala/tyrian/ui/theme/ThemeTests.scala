package tyrian.ui.theme

import tyrian.Style
import tyrian.ui.TextBlock
import tyrian.ui.theme.Theme

class ThemeTests extends munit.FunSuite {

  test("Should render TextBlock with no styles when using Theme.NoStyles") {
    given Theme = Theme.NoStyles

    val actual = TextBlock("Hello World").toElem.toString

    // When Theme.NoStyles is used, an empty style attribute is present
    val expected = """<span style="">Hello World</span>"""

    assertEquals(actual, expected)
  }

  test("Should render TextBlock with styles when using Theme.Styles") {
    given Theme = Theme.default

    val actual = TextBlock("Hello World").toElem.toString

    val expectedStyles = Style(
      "font-family" -> "Arial, sans-serif",
      "font-size"   -> "1rem",
      "font-weight" -> "400",
      "color"       -> "rgba(51, 51, 51, 255)",
      "text-align"  -> "left",
      "line-height" -> "1.5rem",
      "white-space" -> "normal"
    )

    val expected = s"""<span style="${expectedStyles.asString}">Hello World</span>"""

    assertEquals(actual, expected)
  }

  test("Should render heading with no styles when using Theme.NoStyles") {
    given Theme = Theme.NoStyles

    val actual = TextBlock("Title").toHeading1.toElem.toString

    // Even headings get empty style attribute with NoStyles theme
    val expected = """<h1 style="">Title</h1>"""

    assertEquals(actual, expected)
  }

  test("Should render heading with styles when using Theme.Styles") {
    given Theme = Theme.default

    val actual = TextBlock("Title").toHeading1.toElem.toString

    val expectedStyles = Style(
      "font-family" -> "Arial, sans-serif",
      "font-size"   -> "2rem",
      "font-weight" -> "600",
      "color"       -> "rgba(26, 26, 26, 255)",
      "text-align"  -> "left",
      "line-height" -> "1.2rem",
      "white-space" -> "normal"
    )

    val expected = s"""<h1 style="${expectedStyles.asString}">Title</h1>"""

    assertEquals(actual, expected)
  }

  test("Should preserve class names regardless of theme type") {
    val textBlock = TextBlock("Test").withClassNames(Set("custom-class", "another-class"))

    // Test with NoStyles
    {
      given Theme          = Theme.NoStyles
      val actualNoStyles   = textBlock.toElem.toString
      val expectedNoStyles = """<span style="" class="custom-class another-class">Test</span>"""
      assertEquals(actualNoStyles, expectedNoStyles)
    }

    // Test with Styles
    {
      given Theme          = Theme.default
      val actualWithStyles = textBlock.toElem.toString
      val expectedStyles = Style(
        "font-family" -> "Arial, sans-serif",
        "font-size"   -> "1rem",
        "font-weight" -> "400",
        "color"       -> "rgba(51, 51, 51, 255)",
        "text-align"  -> "left",
        "line-height" -> "1.5rem",
        "white-space" -> "normal"
      )
      val expectedWithStyles =
        s"""<span style="${expectedStyles.asString}" class="custom-class another-class">Test</span>"""
      assertEquals(actualWithStyles, expectedWithStyles)
    }
  }

  test("Theme modifications should be ignored with NoStyles") {
    given Theme = Theme.NoStyles

    val actual = TextBlock("Test")
      .withThemeOverride(_.withFontSize(tyrian.ui.datatypes.FontSize.Large))
      .toElem
      .toString

    // Theme override should have no effect with NoStyles (empty style attribute)
    val expected = """<span style="">Test</span>"""

    assertEquals(actual, expected)
  }

  test("Theme modifications should work with Styles theme") {
    given Theme = Theme.default

    val actual = TextBlock("Test")
      .withThemeOverride(_.withFontSize(tyrian.ui.datatypes.FontSize.Large))
      .toElem
      .toString

    val expectedStyles = Style(
      "font-family" -> "Arial, sans-serif",
      "font-size"   -> "1.125rem", // Large font size
      "font-weight" -> "400",
      "color"       -> "rgba(51, 51, 51, 255)",
      "text-align"  -> "left",
      "line-height" -> "1.5rem",
      "white-space" -> "normal"
    )

    val expected = s"""<span style="${expectedStyles.asString}">Test</span>"""

    assertEquals(actual, expected)
  }

  test("Different text variants should all respect NoStyles") {
    given Theme = Theme.NoStyles

    val variants = List(
      (TextBlock("Normal").toNormal, "span", "Normal"),
      (TextBlock("Caption").toCaption, "span", "Caption"),
      (TextBlock("Code").toCode, "code", "Code"),
      (TextBlock("Label").toLabel, "label", "Label")
    )

    variants.foreach { case (textBlock, expectedTag, content) =>
      val actual   = textBlock.toElem.toString
      val expected = s"""<$expectedTag style="">$content</$expectedTag>"""
      assertEquals(actual, expected, s"Failed for variant with tag $expectedTag")
    }
  }

}
