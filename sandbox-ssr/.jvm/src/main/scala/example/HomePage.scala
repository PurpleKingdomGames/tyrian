package example

import tyrian.Attribute
import tyrian.Html
import tyrian.Html.*
import tyrian.htmx.Html.*
import tyrian.htmx.Modifier
import tyrian.htmx.Trigger

object HomePage:
  def page(initialList: List[(String, String)]): Html[Nothing] =
    html(
      head(
        script(
          src := "https://unpkg.com/htmx.org@1.9.10",
          Attribute(
            "integrity",
            "sha384-D1Kt99CQMDuVetoL1lrYwg5t+9QdHe7NLX/SoJYkXDFfX37iInKRy5xLSi8nO7UC"
          ),
          Attribute("crossorigin", "anonymous")
        )()
      ),
      body(
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
    )

  def listToHtmlTableRows(list: List[(String, String)]): List[Html[Nothing]] =
    list.map { row =>
      tr(td(row._1), td(row._2))
    }
