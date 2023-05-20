package example

import cats.effect.IO
import org.scalajs.dom.document
import tyrian.Html.*
import tyrian.*
import tyrian.http.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model(""), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.SendText =>
      (model, HttpHelper.callSSR(model.text))

    case Msg.SSRResponse(ssr) =>
      val cmd: Cmd[IO, Msg] =
        Cmd.SideEffect {
          document.getElementById("results").innerHTML = ssr
        }

      (model, cmd)

    case Msg.NewContent(txt) =>
      (model.copy(text = txt), Cmd.None)

    case Msg.NoOp =>
      (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div()(
      input(
        placeholder := "Text to send",
        onInput(s => Msg.NewContent(s))
      ),
      button(onClick(Msg.SendText))("Send it!"),
      br,
      div(id := "results")()
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

  def main(args: Array[String]): Unit =
    launch("myapp")

enum Msg:
  case SendText
  case NewContent(text: String)
  case SSRResponse(html: String)
  case NoOp

final case class Model(text: String)

object HttpHelper:

  val decoder: Decoder[Msg] =
    Decoder[Msg](
      r => Msg.SSRResponse(r.body),
      e => Msg.SSRResponse(e.toString)
    )

  def callSSR(text: String): Cmd[IO, Msg] =
    Http.send(Request.get(s"/ssr/$text"), decoder)
