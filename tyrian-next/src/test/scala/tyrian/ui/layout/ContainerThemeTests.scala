package tyrian.ui.layout

import tyrian.Style
import tyrian.ui.datatypes.BackgroundMode
import tyrian.ui.datatypes.Degrees
import tyrian.ui.datatypes.Fill
import tyrian.ui.datatypes.Position
import tyrian.ui.datatypes.RGBA

class ContainerThemeTests extends munit.FunSuite {

  test("background color style") {
    val theme =
      ContainerTheme.default.withBackgroundColor(RGBA.Red)

    val actual =
      theme.toStyle

    val expected =
      Style("background-color", RGBA.Red.toCSSValue)

    assertEquals(actual.asString, expected.asString)
  }

  test("linear gradient style") {
    val grad =
      Fill.LinearGradient(
        Degrees.`90`,
        RGBA.Black,
        RGBA.White
      )
    val theme =
      ContainerTheme.default.withBackgroundFill(grad)

    val actual =
      theme.toStyle

    val expected =
      Style(
        "background-image",
        s"linear-gradient(${Degrees(90).toCSSValue}, ${RGBA.Black.toCSSValue}, ${RGBA.White.toCSSValue})"
      )

    assertEquals(actual.asString, expected.asString)
  }

  test("radial gradient style") {
    val grad =
      Fill.RadialGradient(
        Position.Center,
        RGBA.Black,
        RGBA.White
      )
    val theme =
      ContainerTheme.default.withBackgroundFill(grad)

    val actual =
      theme.toStyle

    val expected =
      Style(
        "background-image",
        s"radial-gradient(circle at ${Position.Center.toCSSValue}, ${RGBA.Black.toCSSValue}, ${RGBA.White.toCSSValue})"
      )

    assertEquals(actual.asString, expected.asString)
  }

  // image fill + background mode
  test("image fill with background mode") {
    val imgFill =
      Fill.Image("/assets/bg.jpg").withPosition(Position.TopLeft).withMode(BackgroundMode.containNoRepeat)

    val theme =
      ContainerTheme.default.withBackgroundFill(imgFill)

    val actual =
      theme.toStyle

    val expected =
      Style(
        "background-image"    -> "url('/assets/bg.jpg')",
        "background-position" -> Position.TopLeft.toCSSValue
      ) |+| BackgroundMode.containNoRepeat.toStyle

    assertEquals(actual.asString, expected.asString)
  }
}
