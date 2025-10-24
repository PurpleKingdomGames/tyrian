package example

import cats.effect.IO
import example.models.Fruit
import example.models.HttpDetails
import example.models.Page
import example.models.Time
import tyrian.websocket.*

import scala.scalajs.js

final case class Model(
    page: Page,
    echoSocket: Option[WebSocket[IO]],
    socketUrl: String,
    field: String,
    components: List[Counter.Model],
    log: List[String],
    error: Option[String],
    tmpSaveData: String,
    saveData: Option[String],
    currentTime: js.Date,
    http: HttpDetails,
    time: Time,
    mousePosition: (Int, Int),
    flavour: Option[String],
    fruit: List[Fruit],
    fruitInput: String,
    loadedImage: Option[String],
    loadedText: Option[String],
    loadedBytes: Option[IArray[Byte]],
    imageLoadingUrl: String,
    imageLoadingError: Option[String]
)

object Model:
  // val echoServer = "ws://ws.ifelse.io" // public echo server
  val echoServer = "ws://localhost:8080/wsecho"

  val init: Model =
    Model(
      Page.Page1,
      None,
      echoServer,
      "",
      Nil,
      Nil,
      None,
      "",
      None,
      new js.Date(),
      HttpDetails.initial,
      Time(0.0d, 0.0d),
      (0, 0),
      None,
      Nil,
      "",
      None,
      None,
      None,
      "https://openmoji.org/php/download_asset.php?type=emoji&emoji_hexcode=1F9A6&emoji_variant=color",
      None
    )

  // We're only saving/loading the input field contents as an example
  def encode(model: Model): String = model.field
  def decode: Option[String] => Either[String, Model] =
    case None => Left("No snapshot found")
    case Some(data) =>
      Right(Model.init.copy(field = data))
