package example

import tyrian.Html
import tyrian.Html.*
import tyrian.htmx.Html.*
import tyrian.htmx.Modifier
import tyrian.htmx.Trigger

object SearchPage:
  def page(initialList: List[(String, String)]): Html[Nothing] =
    div(
      id      := "tab-content",
      role    := "tabpanel",
      `class` := "tab-content"
    )(
      input(
        `type` := "text",
        name   := "q",
        hxGet  := "/search",
        hxTrigger := List(
          Trigger(tyrian.htmx.Event.Input)
            .withModifiers(Modifier.Changed, Modifier.Delay("300ms")),
          Trigger(tyrian.htmx.Event.KeyDown)
            .withFilter("key=='Enter'")
            .withModifiers(Modifier.From("body"))
        ),
        hxTarget    := "#search-results",
        placeholder := "Search..."
      ),
      table(`class` := "table")(
        thead(
          tr(th("Name"), th("Binomial Name"))
        ),
        tbody(id := "search-results")(
          listToHtmlTableRows(initialList)
        )
      )
    )

  def listToHtmlTableRows(list: List[(String, String)]): List[Html[Nothing]] =
    list.map { row =>
      tr(td(row._1), td(row._2))
    }
