package example

import tyrian.Html
import tyrian.Html.*
import tyrian.SVG.*
import tyrian.htmx.Html.*

object WebSocketsPage:

  def page: Html[Nothing] =
    div(hxExt := "ws", wsConnect := "/ws")(
      div(id := "content")(),
      button(name := "reset", wsSend)("Reset")
    )

  def drawGameOfLife(game: GameOfLife): Html[Nothing] =
    val cellSize = 10
    val cells = (for {
      row    <- 0 until game.height
      column <- 0 until game.width
      isLive = game.cells.contains(Point(column, row))
      color  = if isLive then "#000000" else "#AAAAAA"
    } yield rect(
      width  := cellSize - 1,
      height := cellSize - 1,
      x      := (column * cellSize).toString(),
      y      := (row * cellSize).toString(),
      fill   := color
    )).toList

    div(id := "content", hxSwapOob := "morphdom")(
      svg(width := game.width * cellSize, height := game.height * cellSize)(
        cells
      )
    )
