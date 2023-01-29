package example

import cats.effect.IO
import cats.syntax.either.*
import io.circe.parser.*
import tyrian.Html.*
import tyrian.*
import tyrian.http.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model("cats", "waiting.gif"), HttpHelper.getRandomGif("cats"))

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.MorePlease     => (model, HttpHelper.getRandomGif(model.topic))
    case Msg.NewGif(newUrl) => (model.copy(gifUrl = newUrl), Cmd.None)
    case Msg.GifError(_)    => (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div()(
      h2()(text(model.topic)),
      button(onClick(Msg.MorePlease))(text("more please")),
      br,
      img(src := model.gifUrl)
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

enum Msg:
  case MorePlease              extends Msg
  case NewGif(result: String)  extends Msg
  case GifError(error: String) extends Msg

object Msg:
  private val onResponse: Response => Msg = { response =>
    val deserialised =
      parse(response.body)
        .leftMap(_.message)
        .flatMap {
          _.hcursor
            .downField("data")
            .downField("images")
            .downField("downsized_medium")
            .get[String]("url")
            .toOption
            .toRight("wrong json format")
        }

    deserialised match
      case Left(e)  => Msg.GifError(e)
      case Right(r) => Msg.NewGif(r)
  }

  private val onError: HttpError => Msg =
    e => Msg.GifError(e.toString)

  def fromHttpResponse: Decoder[Msg] =
    Decoder[Msg](onResponse, onError)

final case class Model(topic: String, gifUrl: String)

object HttpHelper:

  def getRandomGif(topic: String): Cmd[IO, Msg] =
    val url =
      s"https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=$topic"
    Http.send(Request.get(url), Msg.fromHttpResponse)
