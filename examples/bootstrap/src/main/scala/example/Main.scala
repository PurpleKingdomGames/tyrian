package example

import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) = (0, Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Increment => (model + 1, Cmd.Empty)
      case Msg.Decrement => (model - 1, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    div(`class` := "container")(
      div(`class` := "row")(
        div(`class` := "col bodyText", styles("text-align" -> "right"))(
          button(onClick(Msg.Decrement))(text("-"))
        ),
        div(`class` := "col bodyText", styles("text-align" -> "center"))(
          text(model.toString)
        ),
        div(`class` := "col bodyText", styles("text-align" -> "left"))(
          button(onClick(Msg.Increment))(text("+"))
        )
      )
    )

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

type Model = Int

enum Msg:
  case Increment, Decrement
