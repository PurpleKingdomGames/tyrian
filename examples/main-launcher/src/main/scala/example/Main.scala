package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

object Main {
  def main(args: Array[String]): Unit =
    TyrianApp.onLoad(
      "CounterApp" -> CounterApp,
      "ChatApp" -> ChatApp
    )
}