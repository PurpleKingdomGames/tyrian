package example

import cats.syntax.either._
import io.circe.parser._
import org.scalajs.dom.document
import org.scalajs.dom.raw.XMLHttpRequest
import scalm.Html._
import scalm._

object Main extends App {

  def main(args: Array[String]): Unit = Scalm.start(this, document.body)

  // MODEL

  final case class Model(topic: String, gifUrl: String)
  type HttpError = String

  def init: (Model, Cmd[Msg]) = (Model("cats", "waiting.gif"), getRandomGif("cats"))

  sealed trait Msg
  case object MorePlease extends Msg
  final case class NewGif(result: Either[HttpError, String]) extends Msg

  // UPDATE

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match {
      case MorePlease            => (model, getRandomGif(model.topic))
      case NewGif(Right(newUrl)) => (model.copy(gifUrl = newUrl), Cmd.Empty)
      case NewGif(Left(_))       => (model, Cmd.Empty)
    }

  // VIEW

  def view(model: Model): Html[Msg] =
    div()(
      h2()(text(model.topic)),
      button(onClick(MorePlease))(text("more please")),
      tag("br")()(),
      tag("img")(attr("src", model.gifUrl))()
    )

  // SUBSCRIPTION

  def subscriptions(model: Model): Sub[Msg] = Sub.Empty

  // HTTP

  def decodeGifUrl(json: String): Either[HttpError, String] =
    parse(json)
      .leftMap(_.message)
      .flatMap { json =>
        json.hcursor
          .downField("data")
          .get[String]("image_url")
          .toOption.toRight("wrong json format")
      }

  def getRandomGif(topic: String): Cmd[Msg] =
    httpGet(s"https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=$topic", x => NewGif(decodeGifUrl(x.responseText)))

  def httpGet(url: String, toMsg: XMLHttpRequest => Msg): Cmd[Msg] =
    Task.RunObservable[XMLHttpRequest, XMLHttpRequest] { observer =>
      val xhr = new XMLHttpRequest
      xhr.open("GET", url)
      xhr.onload = _ => observer.onNext(xhr)
      xhr.onerror = _ => observer.onError(xhr)
      xhr.send(null)
      () => xhr.abort()
    }
      .attempt(_.merge)
      .map(toMsg)
}
