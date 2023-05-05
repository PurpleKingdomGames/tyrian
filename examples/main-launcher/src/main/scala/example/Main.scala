package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

object Main {
  def main(args: Array[String]): Unit =
    TyrianAppF.launchOnContentLoaded(Map(
      "CounterApp" -> (() => CounterApp),
      "ChatApp" -> (() => ChatApp)
    ))
    println("")
}