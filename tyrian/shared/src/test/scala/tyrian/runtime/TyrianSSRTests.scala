package tyrian.runtime

import tyrian.Html.*
import tyrian.*

class TyrianSSRTests extends munit.FunSuite {

  type Model = Int
  type Msg   = Unit

  val model: Model = 0

  test("Can render a p tag") {
    val view: Model => Html[Msg] =
      _ => p(text("Hello, world!"))

    val actual =
      TyrianSSR.render(model, view)

    val expected =
      "<p>Hello, world!</p>"

    assertEquals(actual, expected)
  }

  test("Can include the doctype") {
    val view: Model => Html[Msg] =
      _ => p(text("Hello, world!"))

    val actual =
      TyrianSSR.render(true, model, view)

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
      TyrianSSR.render(html)

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
      TyrianSSR.render(elems)

    val expected =
      "<p>a</p><span>b</span><b>c</b>"

    assertEquals(actual, expected)
  }

  test("Can render attributes") {
    val elems: List[Elem[Msg]] =
      List(
        div(id := "my-div", hidden)(("some content"))
      )

    val actual =
      TyrianSSR.render(elems)

    val expected =
      """<div id="my-div" hidden>some content</div>"""

    assertEquals(actual, expected)
  }

  test("Can render attributes and exclude hidden ones") {
    val elems: List[Elem[Msg]] =
      List(
        div(id := "my-div", hidden(false))(("some content"))
      )

    val actual =
      TyrianSSR.render(elems)

    val expected =
      """<div id="my-div">some content</div>"""

    assertEquals(actual, expected)
  }

  test("Can render a simple page") {
    val elems: Elem[Msg] =
      html(
        head(
          title("My Page")
        ),
        body(
          p(text("Hello, world!"))
        )
      )

    val actual =
      TyrianSSR.render(true, elems)

    val expected =
      "<!DOCTYPE HTML><html><head><title>My Page</title></head><body><p>Hello, world!</p></body></html>"

    assertEquals(actual, expected)
  }

}
