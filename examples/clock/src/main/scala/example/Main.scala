package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.SVG.*
import tyrian.*

import scala.scalajs.js.annotation.*

import scalajs.js
import concurrent.duration.DurationInt

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (new js.Date(), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    msg => (msg.newTime, Cmd.None)

  def view(model: Model): Html[Msg] =
    val angle = model.getSeconds() * 2 * math.Pi / 60 - math.Pi / 2
    val handX = 50 + 40 * math.cos(angle)
    val handY = 50 + 40 * math.sin(angle)
    tag("svg")(viewBox := "0, 0, 100, 100", width := "300px")(
      circle(
        cx   := "50",
        cy   := "50",
        r    := "45",
        fill := "#0B79CE"
      ),
      line(
        x1     := "50",
        y1     := "50",
        x2     := handX.toString,
        y2     := handY.toString,
        stroke := "#023963"
      )
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.every[IO](1.second, "clock-ticks").map(Msg.apply)

type Model = js.Date

final case class Msg(newTime: js.Date)
