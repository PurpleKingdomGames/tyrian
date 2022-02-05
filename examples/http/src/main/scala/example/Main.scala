package example

import io.circe.parser.*
import tyrian.Html.*
import tyrian.*
import tyrian.http.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
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
      img(src := model.gifUrl)
    )

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Empty

enum Msg:
  case MorePlease                 extends Msg
  case NewGif(result: String)     extends Msg
  case GifError(error: HttpError) extends Msg
object Msg:
  def fromHttpResponse: Either[HttpError, String] => Msg =
    case Left(e)  => Msg.GifError(e)
    case Right(s) => Msg.NewGif(s)

final case class Model(topic: String, gifUrl: String)

object HttpHelper:
  private def decodeGifUrl: Http.Decoder[String] =
    Http.Decoder { response =>
      val json = response.body

      val parsed = parse(json) match
        case Right(r) => Right(r)
        case Left(l)  => Left(l.message)

      parsed.flatMap { json =>
        json.hcursor
          .downField("data")
          .downField("images")
          .downField("downsized_medium")
          .get[String]("url")
          .toOption
          .toRight("wrong json format")
      }
    }

  def getRandomGif(topic: String): Cmd[Msg] =
    val url =
      s"https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=$topic"
    Http.send(Request.get(url, decodeGifUrl), Msg.fromHttpResponse)
