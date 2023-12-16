package tyrian

import tyrian.Html.*

class HTMLRenderingTests extends munit.FunSuite {

  type Model = Int
  type Msg   = Unit

  val model: Model = 0

  test("Can render a p tag") {
    val view: Model => Html[Msg] =
      _ => p(text("Hello, world!"))

    val actual =
      view(model).render

    val expected =
      "<p>Hello, world!</p>"

    assertEquals(actual, expected)
  }

  test("Can include the doctype") {
    val view: Model => Html[Msg] =
      _ => p(text("Hello, world!"))

    val actual =
      "<!DOCTYPE HTML>" + view(model)

    val expected =
      "<!DOCTYPE HTML><p>Hello, world!</p>"

    assertEquals(actual, expected)
  }

  test("Can render a div with contents") {
    val html: Html[Msg] =
      div(id := "my-div")(
        p("Hello, world!"),
        span(cls := "my-span-class", style(Style("width" -> "10px", "height" -> "12pt")))(text("test")),
        a(href := "http://tyrian")(text("my link"))
      )

    val actual =
      html.render

    val expected =
      """<div id="my-div"><p>Hello, world!</p><span class="my-span-class" style="width:10px;height:12pt;">test</span><a href="http://tyrian">my link</a></div>"""

    assertEquals(actual, expected)
  }

  test("Can render an arbitrary list of elems") {
    val elems: List[Elem[Msg]] =
      List(
        p(text("a")),
        span(text("b")),
        b(text("c"))
      )

    val actual =
      elems.render

    val expected =
      "<p>a</p><span>b</span><b>c</b>"

    assertEquals(actual, expected)
  }

  test("Can render attributes") {
    val elems: List[Elem[Msg]] =
      List(
        div(id := "my-div", hidden)("some content")
      )

    val actual =
      elems.render

    val expected =
      """<div id="my-div" hidden>some content</div>"""

    assertEquals(actual, expected)
  }

  test("Can render attributes and exclude hidden ones") {
    val elems: List[Elem[Msg]] =
      List(
        div(id := "my-div", hidden(false))("some content")
      )

    val actual =
      elems.render

    val expected =
      """<div id="my-div">some content</div>"""

    assertEquals(actual, expected)
  }

  test("Can render a simple page") {
    val page: Html[Msg] =
      html(
        head(
          title("My Page")
        ),
        body(
          p(text("Hello, world!"))
        )
      )

    val actual =
      "<!DOCTYPE HTML>" + page

    val expected =
      "<!DOCTYPE HTML><html><head><title>My Page</title></head><body><p>Hello, world!</p></body></html>"

    assertEquals(actual, expected)
  }

  test("Can use all overrides of button") {
    val elems: Elem[Msg] =
      div(
        button,
        button(_class := "a"),
        button(List(_class := "a")),
        button(_class := "a")(),
        button(_class := "a")("X"),
        button(List(_class := "a"))("X"),
        button(List(_class := "a"))(text("X")),
        button(_class := "a")(List(text("X"))),
        button(List(_class := "a"))(List(text("X"))),
        button("X"),
        button(text("X")),
        button(List(text("X")))
      )

    val actual =
      elems.render

    val expected =
      """<div><button></button><button class="a"></button><button class="a"></button><button class="a"></button><button class="a">X</button><button class="a">X</button><button class="a">X</button><button class="a">X</button><button class="a">X</button><button>X</button><button>X</button><button>X</button></div>"""

    assertEquals(actual, expected)
  }

}
