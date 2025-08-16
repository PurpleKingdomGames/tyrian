package tyrian.ui.elements.stateful.input

import tyrian.Style
import tyrian.ui.UIKey
import tyrian.ui.datatypes.BorderRadius
import tyrian.ui.datatypes.BorderStyle
import tyrian.ui.datatypes.BorderWidth
import tyrian.ui.datatypes.FontSize
import tyrian.ui.datatypes.FontWeight
import tyrian.ui.datatypes.RGBA
import tyrian.ui.datatypes.Spacing
import tyrian.ui.theme.Theme

class InputTests extends munit.FunSuite {

  given Theme = Theme.default

  val testKey = UIKey("test-input")

  test("Should be able to render a basic input") {
    val actual = Input(testKey).toElem.toString

    // Should contain basic input structure with default styles
    assert(actual.contains("""<input"""), "Should contain input tag")
    assert(actual.contains("""type="text""""), "Should contain text type")
    assert(actual.contains("""style="""), "Should contain style attribute")
  }

  test("Should render input with placeholder") {
    val actual = Input(testKey)
      .withPlaceholder("Enter text here")
      .toElem
      .toString

    assert(actual.contains("""placeholder="Enter text here""""), "Should contain placeholder")
  }

  test("Should render input with value") {
    val actual = Input(testKey)
      .withValue("test value")
      .toElem
      .toString

    assert(actual.contains("""value="test value""""), "Should contain value")
  }

  test("Should render disabled input") {
    val actual = Input(testKey).disabled.toElem.toString

    assert(actual.contains("""disabled="true""""), "Should contain disabled attribute")
  }

  test("Should render readonly input") {
    val actual = Input(testKey).readOnly.toElem.toString

    assert(actual.contains("""readonly="true""""), "Should contain readonly attribute")
  }

  test("Should apply custom text color") {
    val input = Input(testKey)
      .overrideTheme(
        _.withTextColor(RGBA.Red)
      )

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.textColor, RGBA.Red)
  }

  test("Should apply custom background color") {
    val input =
      Input(testKey)
        .overrideTheme(_.withBackgroundColor(RGBA.Blue))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.backgroundColor, RGBA.Blue)
  }

  test("Should apply custom border color") {
    val input =
      Input(testKey)
        .overrideTheme(_.withBorderColor(RGBA.Green))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border.map(_.color).get, RGBA.Green)
  }

  test("Should apply custom border width") {
    val input =
      Input(testKey)
        .overrideTheme(_.withBorderWidth(BorderWidth.Medium))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border.map(_.width).get, BorderWidth.Medium)
  }

  test("Should apply custom border style") {
    val input =
      Input(testKey)
        .overrideTheme(_.withBorderStyle(BorderStyle.Dashed))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border.map(_.style).get, BorderStyle.Dashed)
  }

  test("Should apply custom border radius") {
    val input =
      Input(testKey)
        .overrideTheme(_.withBorderRadius(BorderRadius.Large))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Large)
  }

  test("Should apply rounded modifier") {
    val input = Input(testKey).overrideTheme(_.rounded)

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Medium)
  }

  test("Should apply square modifier") {
    val input = Input(testKey).overrideTheme(_.square)

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.None)
  }

  test("Should apply no border modifier") {
    val input = Input(testKey).overrideTheme(_.noBorder)

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.border, None)
  }

  test("Should apply custom padding") {
    val input = Input(testKey)
      .overrideTheme(_.withPadding(Spacing.Large))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.padding, Spacing.Large)
  }

  test("Should apply custom font size") {
    val input = Input(testKey)
      .overrideTheme(_.withFontSize(FontSize.Large))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.fontSize, FontSize.Large)
  }

  test("Should apply custom font weight") {
    val input =
      Input(testKey)
        .overrideTheme(_.withFontWeight(FontWeight.Bold))

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.fontWeight, FontWeight.Bold)
  }

  test("Should stack multiple theme modifications") {
    val input =
      Input(testKey)
        .overrideTheme(
          _.withTextColor(RGBA.Red)
            .withBackgroundColor(RGBA.Blue)
            .rounded
            .withPadding(Spacing.Large)
        )

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.textColor, RGBA.Red)
    assertEquals(inputTheme.backgroundColor, RGBA.Blue)
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Medium)
    assertEquals(inputTheme.padding, Spacing.Large)
  }

  test("Should apply theme modifications in different order") {
    val input =
      Input(testKey)
        .overrideTheme(
          _.rounded
            .withTextColor(RGBA.Red)
            .withBackgroundColor(RGBA.Blue)
            .withPadding(Spacing.Large)
        )

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.textColor, RGBA.Red)
    assertEquals(inputTheme.backgroundColor, RGBA.Blue)
    assertEquals(inputTheme.border.map(_.radius).get, BorderRadius.Medium)
    assertEquals(inputTheme.padding, Spacing.Large)
  }

  test("Should preserve all input properties when applying theme modifications") {
    val input =
      Input(testKey)
        .withPlaceholder("Test placeholder")
        .withValue("Test value")
        .disabled
        .overrideTheme(_.withTextColor(RGBA.Red))

    assertEquals(input.placeholder, "Test placeholder")
    assertEquals(input.value, "Test value")
    assertEquals(input.isDisabled, true)
    assertEquals(input.key, testKey)

    val inputTheme = input.applyThemeOverrides(Theme.Default.default).toOption.map(_.input).get
    assertEquals(inputTheme.textColor, RGBA.Red)
  }

  test("Should handle class names correctly") {
    val input =
      Input(testKey)
        .withClassNames("custom-class", "another-class")
        .overrideTheme(_.withTextColor(RGBA.Blue))

    assertEquals(input.classNames, Set("custom-class", "another-class"))

    val actual = input.toElem.toString
    assert(
      actual.contains("""class="custom-class another-class"""") ||
        actual.contains("""class="another-class custom-class""""),
      "Should contain class attribute with both classes"
    )
  }

  test("Should generate correct CSS styles for normal state") {
    val inputTheme = InputTheme.default
      .withTextColor(RGBA.Red)
      .withBackgroundColor(RGBA.Blue)
      .solidBorder(BorderWidth.Thin, RGBA.Green)

    val styles = inputTheme.toStyles(Theme.Default.default)

    assert(clue(styles.asString).contains("color:rgba(255, 0, 0, 255)"), "Should contain red text color")
    assert(clue(styles.asString).contains("background-color:rgba(0, 0, 255, 255)"), "Should contain blue background")
    assert(clue(styles.asString).contains("border:0.0625rem solid #00ff00ff"), "Should contain green border")
  }

  test("Should generate correct CSS styles for disabled state") {
    val inputTheme =
      InputTheme.default
        .withDisabledTextColor(RGBA.fromHex("#808080"))
        .withDisabledBackgroundColor(RGBA.fromHex("#d3d3d3"))

    val styles = inputTheme.toDisabledStyles(Theme.Default.default)

    assert(styles.asString.contains("color:rgba(128, 128, 128, 255)"), "Should contain gray text color")
    assert(
      styles.asString.contains("background-color:rgba(211, 211, 211, 255)"),
      "Should contain light gray background"
    )
    assert(styles.asString.contains("cursor:not-allowed"), "Should contain not-allowed cursor")
  }

  test("Should update input state correctly") {
    val input =
      Input(testKey).withValue("initial")

    val updatedResult = input.update(TextInputMsg.Changed(testKey, "new value"))
    assertEquals(updatedResult.unsafeGet.value, "new value")

    val clearedResult = input.update(TextInputMsg.Clear(testKey))
    assertEquals(clearedResult.unsafeGet.value, "")
  }

  test("Should ignore messages for different keys") {
    val input    = Input(testKey).withValue("initial")
    val otherKey = UIKey("other-input")

    val result1 = input.update(TextInputMsg.Changed(otherKey, "new value"))
    assertEquals(result1.unsafeGet.value, "initial")

    val result2 = input.update(TextInputMsg.Clear(otherKey))
    assertEquals(result2.unsafeGet.value, "initial")
  }

}
