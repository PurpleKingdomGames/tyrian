---
title: "Networking"
---

Out of the box, Tyrian supports two flavors of networking, [HTTP and Web Sockets](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples), and we have examples you can run of both. Please see the instructions in the README file.

It should be said that both implementations are quite primitive at the time of writing. Contributions in the form of issues and improvements are very welcome in this area. No doubt they will be improved as the need arises.

## Http

`tyrian.http.Http` is a built-in `Cmd` that defines the following method:

```scala
object Http:
  def send[F[_]: Async, A, Msg](
    request: Request[A],
    resultToMessage: Decoder[Msg]
  ): Cmd[F, Msg]
```

Additionally, Tyrian also integrates with [http4s-dom](https://github.com/http4s/http4s-dom).

### Fetch random GIF via HTTP

Assuming the following imports:

```scala mdoc:js:shared
import cats.effect.IO
import cats.syntax.either.*
import io.circe.HCursor
import io.circe.parser.*
import tyrian.*
import tyrian.cmds.*
import tyrian.http.*
import tyrian.Html.*
```

Let's walk through this example starting with the `Model1` and `Msg1` types.

```scala mdoc:js:shared
final case class Model1(topic: String, gifUrl: String)

enum Msg1:
  case MorePlease              extends Msg1
  case NewGif(result: String)  extends Msg1
  case GifError(error: String) extends Msg1
```

Followed by a `Decoder[Msg1]` needed to parse the HTTP responses.

```scala mdoc:js:shared
object Msg1:
  def jsonDecode(hcursor: HCursor) =
    hcursor
      .downField("data")
      .downField("images")
      .downField("downsized_medium")
      .get[String]("url")
      .toOption
      .toRight("wrong json format")

  private val onResponse: Response => Msg1 = { response =>
    parse(response.body)
      .leftMap(_.message)
      .flatMap(j => jsonDecode(j.hcursor))
      .fold(Msg1.GifError(_), Msg1.NewGif(_))
  }

  private val onError: HttpError => Msg1 =
    e => Msg1.GifError(e.toString)

  def fromHttpResponse: Decoder[Msg1] =
    Decoder[Msg1](onResponse, onError)
```

Next we have an `HttpHelper` that invokes the `Http.send` method using Giphy's API.

```scala mdoc:js:shared
object HttpHelper:
  def url(topic: String) =
    s"https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=$topic"

  def getRandomGif(topic: String): Cmd[IO, Msg1] =
    Http.send(Request.get(url(topic)), Msg1.fromHttpResponse)
```

Ultimately, we can use it in our `init` and `update` methods, or anywhere where a `Cmd[IO, Msg1]` is expected. For example:

```scala mdoc:js
object HttpMain:
  def init(flags: Map[String, String]): (Model1, Cmd[IO, Msg1]) =
    (Model1("cats", "waiting.gif"), HttpHelper.getRandomGif("cats"))

  def update(model: Model1): Msg1 => (Model1, Cmd[IO, Msg1]) =
    case Msg1.MorePlease     => (model, HttpHelper.getRandomGif(model.topic))
    case Msg1.NewGif(newUrl) => (model.copy(gifUrl = newUrl), Cmd.None)
    case Msg1.GifError(_)    => (model, Cmd.None)
```

You can find the full code on the examples directory linked at the top.

### Http4s-dom integration

To use `http4s-dom` instead, we only need to replace the `HttpHelper` with the following implementation.

```scala mdoc:js
import io.circe.{ Decoder as JsonDecoder, DecodingFailure }
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dom.FetchClientBuilder

object Http4sDomHelper:
  private val client = FetchClientBuilder[IO].create

  given JsonDecoder[Msg1] = JsonDecoder.instance { c =>
    Msg1.jsonDecode(c).map(Msg1.NewGif(_)).leftMap(e => DecodingFailure(e, c.history))
  }

  def getRandomGif(topic: String): Cmd[IO, Msg1] =
    val fetchGif: IO[Msg1] =
      client
        .expect[Msg1](HttpHelper.url(topic))
        .handleError(e => Msg1.GifError(e.getMessage))

    Cmd.Run(fetchGif)(identity)
```

## Web Sockets

Another built-in command is `tyrian.websocket.WebSocket`, which has a more complex API.

```scala
final class WebSocket[F[_]: Async](liveSocket: LiveSocket[F]):
  def disconnect[Msg]: Cmd[F, Msg]
  def publish[Msg](message: String): Cmd[F, Msg]
  def subscribe[Msg](f: WebSocketEvent => Msg): Sub[F, Msg]

/** The running instance of the WebSocket */
final class LiveSocket[F[_]: Async](val socket: dom.WebSocket, val subs: Sub[F, WebSocketEvent])

object WebSocket:
  def connect[F[_]: Async, Msg](
    address: String,
    onOpenMessage: String,
    keepAliveSettings: KeepAliveSettings
  )(
    resultToMessage: WebSocketConnect[F] => Msg
  ): Cmd[F, Msg]
```

Having a `WebSocket` instance allows us to `publish` messages (`Cmd[F, Msg]`), `subscribe` to events (`Sub[F, Msg]`), and explicitly `disconnect` (another `Cmd[F, Msg]`) from the server. To initiate a connection, we can use any of the available `connect` methods defined on its companion object.

### WS echo server

The following example demonstrates the usage of `WebSocket`, starting with the following imports:

```scala mdoc:js:shared
import cats.effect.IO
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger
import tyrian.websocket.*
```

Next we have the `Msg` and `Model` types.

```scala mdoc:js:shared
enum Msg:
  case FromSocket(message: String)
  case ToSocket(message: String)
  case WebSocketStatus(status: EchoSocket.Status)

final case class Model(echoSocket: EchoSocket, log: List[String])

object Model:
  val init: Model =
    Model(EchoSocket.init, Nil)
```

Followed by a custom `EchoSocket` class that handles the connection by reacting to `EchoSocket.Status` messages. The handling of the `Connecting` message is of particular interest, as it initiates the socket connection via `WebSocket.connect`, including keep-alive settings.

```scala mdoc:js:shared
final case class EchoSocket(socketUrl: String, socket: Option[WebSocket[IO]]):

  def connectDisconnectButton =
    if socket.nonEmpty then
      button(onClick(EchoSocket.Status.Disconnecting.asMsg))("Disconnect")
    else button(onClick(EchoSocket.Status.Connecting.asMsg))("Connect")

  def update(status: EchoSocket.Status): (EchoSocket, Cmd[IO, Msg]) =
    status match
      case EchoSocket.Status.ConnectionError(err) =>
        (this, Logger.error(s"Failed to open WebSocket connection: $err"))

      case EchoSocket.Status.Connected(ws) =>
        (this.copy(socket = Some(ws)), Cmd.None)

      case EchoSocket.Status.Connecting =>
        val connect =
          WebSocket.connect[IO, Msg](
            address = socketUrl,
            onOpenMessage = "Connect me!",
            keepAliveSettings = KeepAliveSettings.default
          ) {
            case WebSocketConnect.Error(err) =>
              EchoSocket.Status.ConnectionError(err).asMsg

            case WebSocketConnect.Socket(ws) =>
              EchoSocket.Status.Connected(ws).asMsg
          }

        (this, connect)

      case EchoSocket.Status.Disconnecting =>
        val log = Logger.info[IO]("Graceful shutdown of EchoSocket connection")
        val cmds =
          socket.map(ws => Cmd.Batch(log, ws.disconnect)).getOrElse(log)

        (this.copy(socket = None), cmds)

      case EchoSocket.Status.Disconnected =>
        (this, Logger.info("WebSocket not connected yet"))

  def publish(message: String): Cmd[IO, Msg] =
    socket.map(_.publish(message)).getOrElse(Cmd.None)

  def subscribe(toMessage: WebSocketEvent => Msg): Sub[IO, Msg] =
    socket.fold(Sub.emit[IO, Msg](EchoSocket.Status.Disconnected.asMsg)) {
      _.subscribe(toMessage)
    }

object EchoSocket:

  val init: EchoSocket =
    EchoSocket("wss://ws.ifelse.io/", None)

  enum Status:
    case Connecting
    case Connected(ws: WebSocket[IO])
    case ConnectionError(msg: String)
    case Disconnecting
    case Disconnected

    def asMsg: Msg = Msg.WebSocketStatus(this)
```

At last, we can see how to handle socket messages and status changes in our `update` method.

```scala mdoc:js
def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
  case Msg.WebSocketStatus(status) =>
    val (nextWS, cmds) = model.echoSocket.update(status)
    (model.copy(echoSocket = nextWS), cmds)

  case Msg.FromSocket(message) =>
    val logWS = Logger.info[IO]("Got: " + message)
    (model.copy(log = message :: model.log), logWS)

  case Msg.ToSocket(message) =>
    val cmds: Cmd[IO, Msg] =
      Cmd.Batch(
        Logger.info("Sent: " + message),
        model.echoSocket.publish(message)
      )

    (model, cmds)
```

Moreover, the `subscriptions` method handles Web Socket events.

```scala mdoc:js
  def subscriptions(model: Model): Sub[IO, Msg] =
    model.echoSocket.subscribe {
      case WebSocketEvent.Error(errorMesage) =>
        Msg.FromSocket(errorMesage)

      case WebSocketEvent.Receive(message) =>
        Msg.FromSocket(message)

      case WebSocketEvent.Open =>
        Msg.FromSocket("<no message - socket opened>")

      case WebSocketEvent.Close(code, reason) =>
        Msg.FromSocket(s"<socket closed> - code: $code, reason: $reason")

      case WebSocketEvent.Heartbeat =>
        Msg.ToSocket("<💓 heartbeat 💓>")
    }
```

The full code can be found on the examples linked at the top. 

Furthermore, you may also find the [trading project](https://github.com/gvolpe/trading/tree/main/modules/ws-client) useful: a full-stack application with a Web Socket client sharing the back-end domain model.
