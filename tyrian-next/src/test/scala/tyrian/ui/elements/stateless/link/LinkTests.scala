package tyrian.ui.elements.stateless.link

import tyrian.Tag
import tyrian.next.GlobalMsg
import tyrian.ui.Target
import tyrian.ui.TextBlock
import tyrian.ui.theme.Theme

class LinkTests extends munit.FunSuite {

  case object DoSomething extends GlobalMsg

  given Theme =
    Theme.NoStyles

  test("Should be able to render a link: url") {
    val actual =
      Link("./an-image.png") {
        TextBlock("Link text!")
      }.toElem.toString

    val expected =
      """<a href="./an-image.png"><span style="">Link text!</span></a>"""

    assertEquals(actual, expected)
  }

  test("Should be able to render a link: url + target") {
    val actual =
      Link("./an-image.png") {
        TextBlock("Link text!")
      }.withTarget(Target.Blank).toElem.toString

    val expected =
      """<a href="./an-image.png" target="_blank"><span style="">Link text!</span></a>"""

    assertEquals(actual, expected)
  }

  test("Should be able to render a link: onClick") {
    val actual =
      Link(DoSomething) {
        TextBlock("Link text!")
      }.toElem

    // onClick's are not rendered as they're intercepted by the Tyrian runtime.
    val expected =
      """<a><span style="">Link text!</span></a>"""

    assertEquals(actual.toString, expected)

    actual match
      case Tag(_, List(_, _, e: tyrian.Event[?, ?], _), _, _) =>
        assertEquals(e.name, "click")

      case _ =>
        fail("Expected a tag")
  }

  test("Should be able to render a link: url + onClick") {
    val actual =
      Link("./an-image.png") {
        TextBlock("Link text!")
      }.onClick(DoSomething).toElem

    // onClick's are not rendered as they're intercepted by the Tyrian runtime.
    val expected =
      """<a href="./an-image.png"><span style="">Link text!</span></a>"""

    assertEquals(actual.toString, expected)

    actual match
      case Tag(_, List(_, _, e: tyrian.Event[?, ?], _), _, _) =>
        assertEquals(e.name, "click")

      case _ =>
        fail("Expected a tag")
  }

}
