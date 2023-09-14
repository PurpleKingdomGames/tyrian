package tyrian

import tyrian.Html.*

class AttrTests extends munit.FunSuite {

  test("hidden can be made standalone") {
    val attr = hidden
    assert(clue(attr.name) == "hidden")
  }

  test("event properties propagate through map") {
    val event1 = Event("test", _ => (), true, true, true)
    val event2 = Event("test", _ => (), false, false, false)

    val mapped1 = event1.map(identity)
    val mapped2 = event2.map(identity)

    (event1, mapped1) match
      case (Event(a1, _, b1, c1, d1), Event(a2, _, b2, c2, d2)) =>
        assert(clue(a1 == a2))
        assert(clue(b1 == b2))
        assert(clue(c1 == c2))
        assert(clue(d1 == d2))

    (event2, mapped2) match
      case (Event(a1, _, b1, c1, d1), Event(a2, _, b2, c2, d2)) =>
        assert(clue(a1 == a2))
        assert(clue(b1 == b2))
        assert(clue(c1 == c2))
        assert(clue(d1 == d2))
  }

}
