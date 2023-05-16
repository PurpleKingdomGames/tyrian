package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*
import scala.util.Try

object CounterApp extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    val initialValue: Option[Int] = for {
      initialCounter <- flags.get("InitialCounter")
      initialCounterInt <- Try(initialCounter.toInt).toOption
    } yield initialCounterInt
    (Model(initialValue.getOrElse(0)), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Increment => (model + 1, Cmd.None)
    case Msg.Decrement => (model - 1, Cmd.None)
    case Msg.NavigateTo => (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))("-"),
      div(model.toString),
      button(onClick(Msg.Increment))("+")
    )

  def router: Location => Msg =
    _ => Msg.NavigateTo

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

opaque type Model = Int
object Model:
  def apply(value: Int): Model = value
  
  extension (i: Model)
    def +(other: Int): Model = i + other
    def -(other: Int): Model = i - other

enum Msg:
  case Increment, Decrement, NavigateTo
