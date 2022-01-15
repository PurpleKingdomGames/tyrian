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

  test("Can render a div with contents") {
    val html: Html[Msg] =
      div(id("my-div"))(
        p(text("Hello, world!")),
        span(cls("my-span-class"), style(Style("width" -> "10px", "height" -> "12pt")))(text("test")),
        a(href("http://tyrian"))(text("my link"))
      )

    val actual =
      TyrianSSR.render(html)

    val expected =
      """<div id="my-div"><p>Hello, world!</p><span class="my-span-class" style="width:10px;height:12pt;">test</span><a href="http://tyrian">my link</a></div>"""

    assertEquals(actual, expected)
  }

}
