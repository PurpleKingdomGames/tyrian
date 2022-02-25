package tyrian

import org.scalajs.dom.document
import snabbdom.VNode
import tyrian.Html.*
import tyrian.runtime.TyrianRuntime

class AttrTests extends munit.FunSuite {

  test("hidden can be made standalone") {
    val attr = hidden
    assert(clue(attr.name) == "hidden")
  }

}
