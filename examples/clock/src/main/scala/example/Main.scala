package example

import scalm.{Cmd, Html, Scalm, Sub}
import scalm.Html._
import org.scalajs.dom.document

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
    tag("svg")(attr("viewBox", "0, 0, 100, 100"), attr("width", "300px"))(
      tag("circle")(attr("cx", "50"), attr("cy", "50"), attr("r", "45"), attr("fill", "#0B79CE"))(),
      tag("line")(attr("x1", "50"), attr("y1", "50"), attr("x2", handX.toString), attr("y2", handY.toString), attr("stroke", "#023963"))()
    )
  }

  def subscriptions(model: Model): Sub[Msg] =
    Sub.every(1.second, "clock-ticks").map(Msg.apply)

  def main(args: Array[String]): Unit =
    Scalm.start(document.body, init, update, view, subscriptions)

final case class Msg(newTime: js.Date)
