package tyrian.next

import tyrian.Html.*

class HtmlFragmentTests extends munit.FunSuite {

  test("append") {
    val fragment = HtmlFragment(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val additionalHtml = Batch(
      p("New paragraph"),
      span("New span")
    )

    val actual = fragment.append(additionalHtml)

    val expected = HtmlFragment(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      ),
      p("New paragraph"),
      span("New span")
    )

    assertEquals(actual, expected)
  }

  test("replace") {
    val fragment = HtmlFragment(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val newEntries = Batch(
      p("New paragraph"),
      span("New span")
    )

    val actual = fragment.replace(newEntries)

    val expected = HtmlFragment(
      p("New paragraph"),
      span("New span")
    )

    assertEquals(actual, expected)
  }

  test("toHtml") {
    val fragment = HtmlFragment(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val actual =
      fragment.toHtmlRoot.toHtml

    val expected =
      div(
        div(id := "a")(
          div(id := "b")(),
          div(id := "c")()
        )
      )

    assertEquals(actual, expected)
  }

  test("combine") {
    val frag1 =
      HtmlFragment(
        p("a"),
        p("b")
      ).insert(MarkerId("1"), b("1"))

    val frag2 =
      HtmlFragment(
        p("c")
      ).insert(MarkerId("2"), b("2"))

    val actual =
      frag1 |+| frag2

    val expected =
      HtmlFragment(
        Batch(
          p("a"),
          p("b"),
          p("c")
        ),
        Map(
          MarkerId("1") -> Batch(b("1")),
          MarkerId("2") -> Batch(b("2"))
        )
      )

    assertEquals(actual, expected)
  }

}
