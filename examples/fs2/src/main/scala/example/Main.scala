package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (List("A message will appear here every 5 seconds."), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Tick(msg) => (model :+ msg, Cmd.None)
    case Msg.NoOp      => (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div(
      model.map { msg =>
        p(msg)
      }
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.make(
      "pulse",
      fs2.Stream
        .awakeEvery[IO](5.seconds)
        .map(t => Msg.Tick("5 second pulse at " + t.toString))
    )

type Model = List[String]

enum Msg:
  case Tick(msg: String)
  case NoOp
