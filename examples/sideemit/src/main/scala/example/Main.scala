package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (List.empty[String], Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Add(msg) =>
      (msg :: model, Cmd.Run(IO.sleep(5.seconds) *> IO(Msg.Remove(msg)), identity))
    case Msg.Remove(msg) =>
      (model.filterNot(_ == msg), Cmd.None)

  def item(id: String) =
    button(onClick(Msg.Add(id)))(id)


  def view(model: Model): Html[Msg] =
    div(
      item("one"),
      item("two"),
      item("three"),
      item("four"),
      ul(model.map(item => li(item)))
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

type Model = List[String]

enum Msg:
  case Add(msg: String)
  case Remove(msg: String)
