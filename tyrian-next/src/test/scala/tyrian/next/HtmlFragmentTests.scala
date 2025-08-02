package tyrian.next

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

  test("merging markers within markers") {

    val markerId  = MarkerId("here")
    val markerId2 = MarkerId("here2")

    val root =
      HtmlFragment(
        div(id := "a")(
          div(id := "b")(
            Marker(markerId, p("Hello, world!"))
          ),
          div(id := "c")()
        )
      )

    val next =
      HtmlFragment.Insert(
        markerId,
        p("New content"),
        div(
          Marker(markerId2)
        )
      )

    val next2 =
      HtmlFragment.Insert(
        markerId2,
        p("New content 2")
      )

    val actual =
      root |+| next |+| next2

    val expected =
      HtmlFragment(
        div(id := "a")(
          div(id := "b")(
            Marker(
              markerId,
              p("Hello, world!"),
              p("New content"),
              div(
                Marker(
                  markerId2,
                  p("New content 2")
                )
              )
            )
          ),
          div(id := "c")()
        )
      )

    assertEquals(actual, expected)
  }

  test("withMarkerId - MarkUp") {
    val markerId = MarkerId("test")
    val actual =
      HtmlFragment
        .MarkUp(
          div(id := "a")(
            div(id := "b")(),
            div(id := "c")()
          )
        )
        .withMarkerId(markerId)

    val expected =
      HtmlFragment.Insert(
        markerId,
        div(id := "a")(
          div(id := "b")(),
          div(id := "c")()
        )
      )

    assertEquals(actual, expected)
  }

  test("withMarkerId - Insert") {
    val markerId = MarkerId("test")
    val actual =
      HtmlFragment
        .Insert(
          MarkerId("test 2"),
          div(id := "a")(
            div(id := "b")(),
            div(id := "c")()
          )
        )
        .withMarkerId(markerId)

    val expected =
      HtmlFragment.Insert(
        markerId,
        div(id := "a")(
          div(id := "b")(),
          div(id := "c")()
        )
      )

    assertEquals(actual, expected)
  }

  test("clearMarkerId") {
    val markerId = MarkerId("test")
    val actual =
      HtmlFragment
        .Insert(
          markerId,
          div(id := "a")(
            div(id := "b")(),
            div(id := "c")()
          )
        )
        .clearMarkerId

    val expected =
      HtmlFragment.MarkUp(
        div(id := "a")(
          div(id := "b")(),
          div(id := "c")()
        )
      )

    assertEquals(actual, expected)
  }

  test("addHtml") {
    val fragment = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val additionalHtml = Batch(
      p("New paragraph"),
      span("New span")
    )

    val actual = fragment.addHtml(additionalHtml)

    val expected = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      ),
      p("New paragraph"),
      span("New span")
    )

    assertEquals(actual, expected)
  }

  test("withHtml") {
    val fragment = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val newEntries = Batch(
      p("New paragraph"),
      span("New span")
    )

    val actual = fragment.withHtml(newEntries)

    val expected = HtmlFragment.MarkUp(
      p("New paragraph"),
      span("New span")
    )

    assertEquals(actual, expected)
  }

  test("toHtml") {
    val fragment = HtmlFragment.MarkUp(
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

  test("combine MarkUp + MarkUp - no markers") {
    val fragment1 = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val fragment2 = HtmlFragment.MarkUp(
      p("New paragraph"),
      span("New span")
    )

    val actual = fragment1 |+| fragment2

    val expected = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      ),
      p("New paragraph"),
      span("New span")
    )

    assertEquals(actual, expected)
  }

  test("combine MarkUp + Insert - no markers") {
    val fragment1 = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val fragment2 = HtmlFragment.Insert(
      MarkerId("marker"),
      p("New paragraph"),
      span("New span")
    )

    val actual = fragment1 |+| fragment2

    val expected = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    assertEquals(actual, expected)
  }

  test("combine Insert + MarkUp - no markers") {
    val fragment1 = HtmlFragment.Insert(
      MarkerId("marker"),
      p("New paragraph"),
      span("New span")
    )

    val fragment2 = HtmlFragment.MarkUp(
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    val actual = fragment1 |+| fragment2

    val expected = HtmlFragment.Insert(
      MarkerId("marker"),
      p("New paragraph"),
      span("New span"),
      div(id := "a")(
        div(id := "b")(),
        div(id := "c")()
      )
    )

    assertEquals(actual, expected)
  }

  test("combine Insert + Insert - no markers") {
    val fragment1 = HtmlFragment.Insert(
      MarkerId("marker1"),
      p("First paragraph"),
      span("First span")
    )

    val fragment2 = HtmlFragment.Insert(
      MarkerId("marker2"),
      p("Second paragraph"),
      span("Second span")
    )

    val actual = fragment1 |+| fragment2

    val expected = HtmlFragment.Insert(
      MarkerId("marker1"),
      p("First paragraph"),
      span("First span")
    )

    assertEquals(actual, expected)
  }

  test("combine Insert + Insert - no markers (same marker id)") {
    val fragment1 = HtmlFragment.Insert(
      MarkerId("marker1"),
      p("First paragraph"),
      span("First span")
    )

    val fragment2 = HtmlFragment.Insert(
      MarkerId("marker1"),
      p("Second paragraph"),
      span("Second span")
    )

    val actual = fragment1 |+| fragment2

    val expected = HtmlFragment.Insert(
      MarkerId("marker1"),
      p("First paragraph"),
      span("First span"),
      p("Second paragraph"),
      span("Second span")
    )

    assertEquals(actual, expected)
  }

}
