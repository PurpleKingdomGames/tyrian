package example

import org.scalajs.dom.document
import tyrian.Cmd
import tyrian.Html
import tyrian.Html._
import tyrian.Sub
import tyrian.Tyrian

import scalajs.js
import concurrent.duration.DurationInt

object Clock:

  opaque type Model = js.Date

  def init: (Model, Cmd[Msg]) =
    (new js.Date(), Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    (msg.newTime, Cmd.Empty)

  def view(model: Model): Html[Msg] = {
    val angle = model.getSeconds() * 2 * math.Pi / 60 - math.Pi / 2
    val handX = 50 + 40 * math.cos(angle)
    val handY = 50 + 40 * math.sin(angle)
    tag("svg")(attributes("viewBox" -> "0, 0, 100, 100", "width" -> "300px"))(
      tag("circle")(attributes("cx" -> "50", "cy" -> "50", "r" -> "45", "fill" -> "#0B79CE"))(),
      tag("line")(
        attributes(
          "x1"     -> "50",
          "y1"     -> "50",
          "x2"     -> handX.toString,
          "y2"     -> handY.toString,
          "stroke" -> "#023963"
        )
      )()
    )
  }

  def subscriptions(model: Model): Sub[Msg] =
    Sub.every(1.second, "clock-ticks").map(Msg.apply)

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view, subscriptions)

final case class Msg(newTime: js.Date)
