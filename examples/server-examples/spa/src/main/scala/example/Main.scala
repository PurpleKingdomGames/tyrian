package example

import org.scalajs.dom.document
import tyrian.Html.*
import tyrian.*
import tyrian.http.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
    (Model(""), Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.SendText =>
        (model, HttpHelper.callSSR(model.text))

      case Msg.SSRResponse(ssr) =>
        val cmd =
          Cmd.SideEffect { () =>
            document.getElementById("results").innerHTML = ssr
          }

        (model, cmd)

      case Msg.NewContent(txt) =>
        (model.copy(text = txt), Cmd.Empty)

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

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

  def main(args: Array[String]): Unit =
    launch("myapp")

enum Msg:
  case SendText                  extends Msg
  case NewContent(text: String)  extends Msg
  case SSRResponse(html: String) extends Msg

final case class Model(text: String)

object HttpHelper:
  def callSSR(text: String): Cmd[Msg] =
    val toMsg: Either[HttpError, String] => Msg = {
      case Left(e)     => Msg.SSRResponse(e.toString)
      case Right(html) => Msg.SSRResponse(html)
    }

    Http.send(Request.get(s"/ssr/$text", Http.Decoder.asString), toMsg)
