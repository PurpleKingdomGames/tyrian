package tyrian

import tyrian.*
import tyrian.Html.*

class HtmlFragmentTests extends munit.FunSuite {

  test("merging markers") {

    val markerId = MarkerId("here")

    val root =
      HtmlFragment(
        div(id := "a")(
          div(id := "b")(
            Marker(markerId)
          ),
          div(id := "c")()
        )
      )

    val next =
      HtmlFragment.Insert(
        markerId,
        p("Hello, world!")
      )

    val actual =
      root |+| next

    val expected =
      HtmlFragment(
        div(id := "a")(
          div(id := "b")(
            Marker(markerId, p("Hello, world!"))
          ),
          div(id := "c")()
        )
      )

    assertEquals(actual, expected)
  }

  // TODO: Missing tests:
  // combine (root + root)
  // combine (root + insert)
  // combine (insert + root)
  // combine (insert + insert)
  // markers within markers
  // withMarkerId
  // clearMarkerId
  // combine
  // |+|
  // addHtml
  // withHtml
  // toHtml

}
