package tyrian.next

import tyrian.Html.*

final class HtmlRootTests extends munit.FunSuite:

  test("resolve - single markup") {
    val frag =
      HtmlFragment(
        p("Hello")
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("Hello")
      )
      
    assertEquals(actual, expected)
  }
  
  test("resolve - multiple markups") {
    val frag =
      HtmlFragment(
        Batch(
          p("a"),
          p("b"),
          p("c")
        )
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("a"),
        p("b"),
        p("c")
      )
      
    assertEquals(actual, expected)
  }

  test("resolve - single markup, no marker, one insert") {
    val frag =
      HtmlFragment(
        p("Hello")
      ).insert(
        MarkerId("1"),
        p("a")
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("Hello")
      )
      
    assertEquals(actual, expected)
  }
  
  test("resolve - single markup, different marker, one insert") {
    val frag =
      HtmlFragment(
        p("Hello"),
        Marker(MarkerId("nope"))
      ).insert(
        MarkerId("1"),
        p("a")
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("Hello")
      )
      
    assertEquals(actual, expected)
  }

  test("resolve - single markup, matching marker, one insert") {
    val frag =
      HtmlFragment(
        p("Hello"),
        Marker(MarkerId("1"))
      ).insert(
        MarkerId("1"),
        p("a")
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("Hello"),
        p("a")
      )
      
    assertEquals(actual, expected)
  }

  test("resolve - single markup, matching markers, two inserts") {
    val frag =
      HtmlFragment(
        p("Hello"),
        Marker(MarkerId("1")),
        Marker(MarkerId("2")),
      ).insert(
        MarkerId("1"),
        p("a")
      ).insert(
        MarkerId("2"),
        p("b")
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("Hello"),
        p("a"),
        p("b")
      )
      
    assertEquals(actual, expected)
  }
  
  test("resolve - single markup, matching marker, two inserts, nested") {
    val frag =
      HtmlFragment(
        p("Hello"),
        Marker(MarkerId("1"))
      ).insert(
        MarkerId("1"),
        p("a"),
        Marker(MarkerId("2"))
      ).insert(
        MarkerId("2"),
        p("b")
      )
    
    val root =
      HtmlRoot(frag)

    val actual =
      root.toHtml

    val expected =
      div(
        p("Hello"),
        p("a"),
        p("b")
      )
      
    assertEquals(actual, expected)
  }
