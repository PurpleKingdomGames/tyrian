package example

import cats.syntax.either._
import io.circe.parser._
import org.scalajs.dom.document
import scalm.Html._
import scalm._
import scalm.http.Http

object Main {

  def main(args: Array[String]): Unit =
    Scalm.start(document.getElementById("myapp"), init, update, view, subscriptions)

  def init: (Model, Cmd[Msg]) =
    (Model("cats", "waiting.gif"), HttpHelper.getRandomGif("cats"))

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.MorePlease            => (model, HttpHelper.getRandomGif(model.topic))
      case Msg.NewGif(Right(newUrl)) => (model.copy(gifUrl = newUrl), Cmd.Empty)
      case Msg.NewGif(Left(_))       => (model, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    div()(
      h2()(text(model.topic)),
      button(onClick(Msg.MorePlease))(text("more please")),
      tag("br")()(),
      tag("img")(attr("src", model.gifUrl))()
    )

  def subscriptions(model: Model): Sub[Msg] = Sub.Empty
}

enum Msg:
  case MorePlease                                     extends Msg
  case NewGif(result: Either[http.HttpError, String]) extends Msg

final case class Model(topic: String, gifUrl: String)

object HttpHelper:
  def decodeGifUrl(json: String): Either[String, String] =
    parse(json)
      .leftMap(_.message)
      .flatMap { json =>
        json.hcursor
          .downField("data")
          .get[String]("image_url")
          .toOption
          .toRight("wrong json format")
      }

  def getRandomGif(topic: String): Cmd[Msg] =
    val url =
      s"https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=$topic"
    Http.send(Msg.NewGif.apply, Http.get(url, decodeGifUrl))
