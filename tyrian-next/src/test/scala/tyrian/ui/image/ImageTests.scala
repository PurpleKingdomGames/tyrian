package tyrian.ui.image

import tyrian.Style
import tyrian.ui.Theme
import tyrian.ui.datatypes.Border
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.RGBA

class ImageTests extends munit.FunSuite {

  given Theme =
    Theme.default

  test("Should be able to render an image") {
    val actual =
      Image("./an-image.png").view.toString

    val expected =
      """<img src="./an-image.png" alt="" style="object-fit:fill;"></img>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the image fit") {
    val actual =
      Image("./an-image.png").cover.view.toString

    val expected =
      """<img src="./an-image.png" alt="" style="object-fit:cover;"></img>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - rounded") {
    val actual =
      Image("./an-image.png").rounded.view.toString

    val styles =
      Style(
        "object-fit"    -> "fill",
        "border"        -> "0 none #000000ff",
        "border-radius" -> "0.25rem"
      )

    val expected =
      s"""<img src="./an-image.png" alt="" style="${styles.asString}"></img>"""

    assertEquals(actual, expected)
  }

  test("should be able to modify the theme - border") {
    val actual =
      Image("./an-image.png")
        .withBorder(Border.solid(BorderWidth.Medium, RGBA.Purple))
        .view
        .toString

    val styles =
      Style(
        "object-fit"    -> "fill",
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0"
      )

    val expected =
      s"""<img src="./an-image.png" alt="" style="${styles.asString}"></img>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - rounded + border") {
    val actual =
      Image("./an-image.png").rounded
        .withBorder(Border.solid(BorderWidth.Medium, RGBA.Purple))
        .view
        .toString

    val styles =
      Style(
        "object-fit"    -> "fill",
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0.25rem"
      )

    val expected =
      s"""<img src="./an-image.png" alt="" style="${styles.asString}"></img>"""

    assertEquals(actual, expected)
  }

  test("should be able to stack theme modifications - border + rounded (reversed)") {
    val actual =
      Image("./an-image.png")
        .solidBorder(BorderWidth.Medium, RGBA.Purple)
        .rounded
        .view
        .toString

    val styles =
      Style(
        "object-fit"    -> "fill",
        "border"        -> "0.125rem solid #a020f0ff",
        "border-radius" -> "0.25rem"
      )

    val expected =
      s"""<img src="./an-image.png" alt="" style="${styles.asString}"></img>"""

    assertEquals(actual, expected)
  }

}
