package example

import cats.syntax.either._
import io.circe.parser._
import org.scalajs.dom.document
import tyrian.Html._
import tyrian._
import tyrian.http.{Http, HttpError}

object Main:

  def init: (Model, Cmd[Msg]) =
    (Model("cats", "waiting.gif"), HttpHelper.getRandomGif("cats"))

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.MorePlease     => (model, HttpHelper.getRandomGif(model.topic))
      case Msg.NewGif(newUrl) => (model.copy(gifUrl = newUrl), Cmd.Empty)
      case Msg.GifError(_)    => (model, Cmd.Empty)

  def view(model: Model): Html[Msg] =
    div()(
      h2()(text(model.topic)),
      button(onClick(Msg.MorePlease))(text("more please")),
      br,
      img(src(model.gifUrl))
    )

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

  def main(args: Array[String]): Unit =
    Tyrian.start(document.getElementById("myapp"), init, update, view, subscriptions)

end Main

enum Msg:
  case MorePlease                      extends Msg
  case NewGif(result: String)          extends Msg
  case GifError(error: http.HttpError) extends Msg
object Msg:
  def fromHttpResponse: Either[HttpError, String] => Msg =
    case Left(e)  => Msg.GifError(e)
    case Right(s) => Msg.NewGif(s)

final case class Model(topic: String, gifUrl: String)

object HttpHelper:
  private def decodeGifUrl(json: String): Either[String, String] =
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
    Http.send(Msg.fromHttpResponse, Http.get(url, decodeGifUrl))
