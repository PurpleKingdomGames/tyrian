package com.example.example

import tyrian.*
import tyrian.Html.*

object HomePage:

  val page: Html[Nothing] =
    html(
      head(
        meta(charset := "UTF-8"),
        title("Tyrian SPA")
      ),
      body(
        div(id := "myapp")(),
        script(typ := "module", src := "./spa.js")()
      )
    )
