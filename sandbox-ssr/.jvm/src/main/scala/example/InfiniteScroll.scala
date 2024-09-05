package example

import cats.Applicative
import cats.implicits.*
import tyrian.Attribute
import tyrian.Html
import tyrian.Html.*
import tyrian.htmx.Html.*

object InfiniteScroll:
  def page[F[_]: Applicative]: F[Html[Nothing]] =
    rowBatch(20, 0).map { rows =>
      div(
        id      := "tab-content",
        role    := "tabpanel",
        `class` := "tab-content"
      )(
        table(hxIndicator := ".htmx-indicator")(
          thead(tr(th("Name"), th("Id"))),
          tbody(
            rows*
          )
        )
      )
    }

  private def row(index: Long, lastRow: Boolean, nextPage: Long): Html[Nothing] =
    val attrs = if lastRow then {
      List(hxGet := s"/scroll?page=$nextPage", hxTrigger := "revealed", hxSwap := "afterend", hxTarget := "this")
    } else {
      List()
    }
    tr(attrs)(
      td("Infinite row"),
      td(index.toString())
    )

  def rowBatch[F[_]: Applicative](rowCount: Long, startIndex: Long): F[List[Html[Nothing]]] =
    (0L until rowCount)
      .map { r =>
        row(r + startIndex, r == rowCount - 1, startIndex / rowCount + 1)
      }
      .toList
      .pure[F]
