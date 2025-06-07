package example

import cats.effect.IO
import tyrian.*
import tyrian.Html.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object SandboxUI extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg =
    Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NoOp =>
      (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    p("Hello")

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

enum Msg:
  case NoOp

final case class Model()

object Model:
  val init: Model =
    Model()
