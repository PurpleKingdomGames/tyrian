package tyrian.ui.elements.stateless.canvas

import tyrian.Style
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.Extent
import tyrian.ui.datatypes.RGBA
import tyrian.ui.theme.Theme

class CanvasTests extends munit.FunSuite {

  given Theme =
    Theme.default

  test("Should be able to render a canvas") {
    val actual =
      Canvas().toElem.toString

    val expected =
      """<canvas></canvas>"""

    assertEquals(actual, expected)
  }

  test("Should be able to render a canvas with an ID") {
    val actual =
      Canvas().withId("gfx").toElem.toString

    val expected =
      """<canvas id="gfx"></canvas>"""

    assertEquals(actual, expected)
  }

  test("Should be able to render a canvas at a specific size") {
    val actual =
      Canvas(Extent.px(800), Extent.px(600)).toElem.toString

    val expected =
      """<canvas width="800px" height="600px"></canvas>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - rounded") {
    val actual =
      Canvas()
        .overrideTheme(_.rounded)
        .toElem
        .toString

    val styles =
      Style(
        "border"        -> "0 none #000000ff",
        "border-radius" -> "0.25rem"
      )

    val expected =
      s"""<canvas style="${styles.asString}"></canvas>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - border") {
    val actual =
      Canvas()
        .overrideTheme(
          _.withBorder(Border.solid(BorderWidth.Medium, RGBA.Purple))
        )
        .toElem
        .toString

    val styles =
      Style(
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0"
      )

    val expected =
      s"""<canvas style="${styles.asString}"></canvas>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - rounded + border") {
    val actual =
      Canvas()
        .overrideTheme(
          _.rounded
            .solidBorder(BorderWidth.Medium, RGBA.Purple)
        )
        .toElem
        .toString

    val styles =
      Style(
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0.25rem"
      )

    val expected =
      s"""<canvas style="${styles.asString}"></canvas>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - border + rounded (reversed)") {
    val actual =
      Canvas()
        .overrideTheme(
          _.solidBorder(BorderWidth.Medium, RGBA.Purple).rounded
        )
        .toElem
        .toString

    val styles =
      Style(
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0.25rem"
      )

    val expected =
      s"""<canvas style="${styles.asString}"></canvas>"""

    assertEquals(actual, expected)
  }

}
