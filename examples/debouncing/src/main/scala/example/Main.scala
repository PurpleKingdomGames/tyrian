package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  val DebouncingMillis: Int = 500
  val TickInterval: Int     = 100

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model("", None), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.UpdateValue(v) =>
      (model.copy(debouncer = Some(v, DebouncingMillis)), Cmd.None)

    case Msg.TimePassed =>
      model.debouncer match
        case Some((v, time)) if time < 0 =>
          (model.copy(value = v, debouncer = None), Cmd.None)

        case Some((v, time)) =>
          (model.copy(debouncer = Some((v, time - TickInterval))), Cmd.None)

        case None => (model, Cmd.None)

    case Msg.NoOp => (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div()(
      input(
        placeholder := "Debounced input",
        onInput(Msg.UpdateValue(_)),
        myStyle
      ),
      div(myStyle)(text(model.value))
    )

  private val myStyle =
    styles(
      "width"      -> "100%",
      "height"     -> "40px",
      "padding"    -> "10px 0",
      "font-size"  -> "2em",
      "text-align" -> "center"
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    model match
      case Model(_, Some(_)) =>
        Sub
          .every[IO](TickInterval.millis, "tick")
          .map(_ => Msg.TimePassed)
      case _ =>
        Sub.None

final case class Model(value: String, debouncer: Option[(String, Int)])

enum Msg:
  case UpdateValue(newValue: String)
  case TimePassed
  case NoOp
