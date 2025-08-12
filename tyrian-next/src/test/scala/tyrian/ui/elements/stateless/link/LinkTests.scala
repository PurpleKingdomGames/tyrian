package tyrian.ui.elements.stateless.link

import tyrian.Tag
import tyrian.next.GlobalMsg
import tyrian.ui.Target
import tyrian.ui.TextBlock
import tyrian.ui.theme.Theme

class LinkTests extends munit.FunSuite {

  case object DoSomething extends GlobalMsg

  test("Should be able to render a link: url") {
    given Theme = Theme.NoStyles

    val actual =
      Link("./an-image.png") {
        TextBlock("Link text!")
      }.toElem.toString

    val expected =
      """<a href="./an-image.png"><span style="">Link text!</span></a>"""

    assertEquals(actual, expected)
  }

  test("Should be able to render a link: url + target") {
    given Theme = Theme.NoStyles

    val actual =
      Link("./an-image.png") {
        TextBlock("Link text!")
      }.withTarget(Target.Blank).toElem.toString

    val expected =
      """<a href="./an-image.png" target="_blank"><span style="">Link text!</span></a>"""

    assertEquals(actual, expected)
  }

  test("Should be able to render a link: onClick") {
    given Theme = Theme.NoStyles

    val actual =
      Link(DoSomething) {
        TextBlock("Link text!")
      }.toElem

    // onClick's are not rendered as they're intercepted by the Tyrian runtime.
    val expected =
      """<a><span style="">Link text!</span></a>"""

    assertEquals(actual.toString, expected)

    actual match
      case Tag(_, List(_, _, e: tyrian.Event[?, ?], _, _), _, _) =>
        assertEquals(e.name, "click")

      case _ =>
        fail("Expected a tag")
  }

  test("Should be able to render a link: url + onClick") {
    given Theme = Theme.NoStyles

    val actual =
      Link("./an-image.png") {
        TextBlock("Link text!")
      }.onClick(DoSomething).toElem

    // onClick's are not rendered as they're intercepted by the Tyrian runtime.
    val expected =
      """<a href="./an-image.png"><span style="">Link text!</span></a>"""

    assertEquals(actual.toString, expected)

    actual match
      case Tag(_, List(_, _, e: tyrian.Event[?, ?], _, _), _, _) =>
        assertEquals(e.name, "click")

      case _ =>
        fail("Expected a tag")
  }

  test("Should render link with default LinkTheme styles") {
    given Theme = Theme.default

    val actual =
      Link("./page.html") {
        TextBlock("Styled Link")
      }.toElem.toString

    assert(actual.contains("color:rgba(0, 102, 204, 255)")) // Link blue
    assert(actual.contains("text-decoration:underline"))
    assert(actual.contains("href=\"./page.html\""))
  }

  test("Should apply custom LinkTheme styles") {
    given Theme = Theme.default

    val actual =
      Link("./page.html") {
        TextBlock("Custom Link")
      }.withThemeOverride(_.withTextColor(tyrian.ui.RGBA.fromHex("#ff0000"))).toElem.toString

    assert(actual.contains("color:rgba(255, 0, 0, 255)")) // Red color
    assert(actual.contains("href=\"./page.html\""))
  }

  test("Should preserve LinkTheme base styling when modifying hover color") {
    given Theme = Theme.default

    val actual =
      Link("./page.html") {
        TextBlock("Hover Link")
      }.withThemeOverride(_.withHoverColor(tyrian.ui.RGBA.fromHex("#00ff00"))).toElem.toString

    assert(actual.contains("color:rgba(0, 102, 204, 255)")) // Original link blue
    assert(actual.contains("text-decoration:underline"))
  }

  test("Should apply multiple LinkTheme modifications") {
    given Theme = Theme.default

    val actual =
      Link("./page.html") {
        TextBlock("Multi-styled Link")
      }.withThemeOverride(theme =>
        theme
          .withTextColor(tyrian.ui.RGBA.fromHex("#800080"))
          .withFontWeight(tyrian.ui.FontWeight.Bold)
          .clearDecoration
      ).toElem
        .toString

    assert(actual.contains("color:rgba(128, 0, 128, 255)")) // Purple
    assert(actual.contains("font-weight:700"))              // Bold
    assert(!actual.contains("text-decoration:underline"))   // No underline
  }

  test("Should chain LinkTheme modifications correctly") {
    given Theme = Theme.default

    val actual =
      Link("./page.html") {
        TextBlock("Chained Link")
      }.withThemeOverride(theme =>
        theme
          .withTextColor(tyrian.ui.RGBA.fromHex("#ff6600"))
          .withFontSize(tyrian.ui.FontSize.Large)
      ).toElem
        .toString

    assert(actual.contains("color:rgba(255, 102, 0, 255)")) // Orange
    assert(actual.contains("font-size:1.125rem"))           // Large font
  }

}
