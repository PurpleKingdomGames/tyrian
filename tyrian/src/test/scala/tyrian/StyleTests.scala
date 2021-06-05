package tyrian

class StyleTests extends munit.FunSuite {

  test("style construction") {

    assertEquals(Style("align", "left").toString, "align:left;")
    assertEquals(Style("align", "").toString, "align;")
    assertEquals(Style("", "left").toString, "left;")
    assertEquals(Style("", "").toString, Style.empty.toString)

  }

  test("styles combine") {
    val styleA = Style("align", "left")
    val styleB = Style("display", "block")

    assertEquals(Style.combine(styleA, styleB).toString, "align:left;display:block;")
  }

  test("styles combineAll") {
    val styleA = Style("align", "left")
    val styleB = Style("display", "block")

    assertEquals(Style.combineAll(List(styleA, styleB)).toString, "align:left;display:block;")
  }

}
