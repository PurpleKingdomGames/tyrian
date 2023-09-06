package tyrian

import tyrian.Html.*

class AttrTests extends munit.FunSuite {

  test("hidden can be made standalone") {
    val attr = hidden
    assert(clue(attr.name) == "hidden")
  }

}
