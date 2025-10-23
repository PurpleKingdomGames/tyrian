package example

import tyrian.Aria.*
import tyrian.Html
import tyrian.Html.*
import tyrian.htmx.Event
import tyrian.htmx.Html.*
import tyrian.htmx.Modifier
import tyrian.htmx.Trigger

object TabbedPage:

  val randomTabs = List("Search", "Infinite Scroll", "WebSockets")

  def page: Html[Nothing] =
    html(
      head(
        script(src := "https://unpkg.com/htmx.org@1.9.10")(),
        script(src := "https://unpkg.com/htmx.org/dist/ext/ws.js")(),
        style("""
              #tab-content.htmx-swapping {
                opacity: 0;
                transition: opacity 1s ease-out;
              }
              #tab-content.htmx-added {
                opacity: 0;
              }
              #tab-content {
                opacity: 1;
                transition: opacity 1s ease-out;
              }""")
      ),
      body(
        div(
          id        := "tabs",
          hxGet     := "/tab/0",
          hxTrigger := Trigger(Event.Load).withModifiers(Modifier.Delay("100ms")), // "load delay:100ms",
          hxTarget  := "#tabs",
          hxSwap    := "innerHTML transition:true"
        )()
      )
    )

  def tabs(tabNames: List[String], selectedTab: Int): Html[Nothing] =
    val tabButtons = tabNames.zipWithIndex.map { case (name, index) =>
      button(
        hxGet        := s"/tab/$index",
        role         := "tab",
        ariaSelected := (selectedTab == index).toString(),
        ariaControls := "tab-content"
      )(name)
    }
    div(
      `class` := "tab-list",
      role    := "tablist"
    )(
      tabButtons
    )

  def emptyContent: Html[Nothing] =
    div(
      id      := "tab-content",
      role    := "tabpanel",
      `class` := "tab-content"
    )()
