package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.SVG.*
import tyrian.*
import tyrian.cmds.Dom
import tyrian.cmds.LocalStorage
import tyrian.cmds.Logger
import tyrian.http.*
import tyrian.syntax.*
import tyrian.websocket.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*
import scala.util.Random

import scalajs.js

@JSExportTopLevel("TyrianApp")
object Sandbox extends MultiPage[Msg, Model]:

  // Here we just do a simple string match, but this could be a route matching
  // lib like: https://github.com/sherpal/url-dsl
  def router: Location => Msg = loc =>
    loc.pathName match
      case "/page2" => Msg.NavigateTo(Page.Page2)
      case "/page3" => Msg.NavigateTo(Page.Page3)
      case "/page4" => Msg.NavigateTo(Page.Page4)
      case "/page5" => Msg.NavigateTo(Page.Page5)
      case "/page6" => Msg.NavigateTo(Page.Page6)
      case _ =>
        println("Unknown route: " + loc.fullPath)
        println(loc)
        Msg.NavigateTo(Page.Page1)

  val hotReloadKey: String = "hotreload"

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    val cmds: Cmd[IO, Msg] =
      Cmd.Batch(
        HotReload.bootstrap(hotReloadKey, Model.decode) {
          case Left(msg)    => Msg.Log("Error during hot-reload!: " + msg)
          case Right(model) => Msg.OverwriteModel(model)
        },
        Logger.info(flags.toString),
        LocalStorage.key(0) {
          case LocalStorage.Result.Key(key) => Msg.Log("Found local storage key: " + key)
          case _                            => Msg.Log("No local storage enties found.")
        },
        LocalStorage.length(l => Msg.Log("Number of local storage entries: " + l.length)),
        Cmd.emit(Msg.Log("Delayed by 0 seconds")),
        Cmd.emitAfterDelay(Msg.Log("Delayed by 10 seconds"), 10.seconds)
      )

    (Model.init, cmds)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.AddFruit =>
      (model.copy(fruit = Fruit(model.fruitInput, false) :: model.fruit), Cmd.None)

    case Msg.UpdateFruitInput(input) =>
      (model.copy(fruitInput = input), Cmd.None)

    case Msg.ToggleFruitAvailability(name) =>
      (
        model.copy(fruit = model.fruit.map { fruit =>
          if fruit.name == name then fruit.copy(available = !fruit.available)
          else fruit
        }),
        Cmd.None
      )

    case Msg.NewFlavour(f) =>
      (model.copy(flavour = Option(f)), Cmd.None)

    case Msg.MouseMove(to) =>
      (model.copy(mousePosition = to), Cmd.None)

    case Msg.UpdateHttpDetails(newUrl) =>
      (model.copy(http = model.http.copy(url = Option(newUrl))), Cmd.None)

    case Msg.UpdateHttpBody(newBody) =>
      (model.copy(http = model.http.copy(body = newBody)), Cmd.None)

    case Msg.UpdateHttpMethod(newMethod) =>
      val method = newMethod match {
        case "get"     => Method.Get
        case "post"    => Method.Post
        case "put"     => Method.Put
        case "patch"   => Method.Patch
        case "delete"  => Method.Delete
        case "options" => Method.Options
        case "head"    => Method.Head
      }

      (model.copy(http = model.http.copy(method = method)), Cmd.None)

    case Msg.UpdateHttpCredentials(newCredentials) =>
      val credentials = newCredentials match {
        case "omit"        => RequestCredentials.Omit
        case "same-origin" => RequestCredentials.SameOrigin
        case "include"     => RequestCredentials.Include
      }
      (model.copy(http = model.http.copy(credentials = credentials)), Cmd.None)

    case Msg.UpdateHttpTimeout(newTimeout) =>
      (model.copy(http = model.http.copy(timeout = newTimeout.toDouble)), Cmd.None)

    case Msg.UpdateHeaderKey(headerIndex, key) =>
      val newHeader = model.http.headers(headerIndex).copy(_1 = key)
      (model.copy(http = model.http.copy(headers = model.http.headers.updated(headerIndex, newHeader))), Cmd.None)

    case Msg.UpdateHeaderValue(headerIndex, value) =>
      val newHeader = model.http.headers(headerIndex).copy(_2 = value)
      (model.copy(http = model.http.copy(headers = model.http.headers.updated(headerIndex, newHeader))), Cmd.None)

    case Msg.UpdateHeaderAdd =>
      (model.copy(http = model.http.copy(headers = model.http.headers :+ ("", ""))), Cmd.None)

    case Msg.UpdateHeaderRemove(headerIndex) =>
      val headers = model.http.headers.patch(headerIndex, Nil, 1)
      (model.copy(http = model.http.copy(headers = headers)), Cmd.None)

    case Msg.UpdateHttpCache(newCache) =>
      val cache = newCache match {
        case "default"        => RequestCache.Default
        case "no-store"       => RequestCache.NoStore
        case "reload"         => RequestCache.Reload
        case "no-cache"       => RequestCache.NoCache
        case "force-cache"    => RequestCache.ForceCache
        case "only-if-cached" => RequestCache.OnlyIfCached
      }
      (model.copy(http = model.http.copy(cache = cache)), Cmd.None)

    case Msg.MakeHttpRequest =>
      val cmd: Cmd[IO, Msg] =
        model.http.url match
          case None =>
            Logger.info("No url entered, skipping Http request.")

          case Some(url) =>
            Cmd.Batch(
              Logger.info(s"Making ${model.http.method.asString} request to: $url"),
              Http.send(
                Request(
                  model.http.method,
                  model.http.headers.map(h => Header(h._1, h._2)),
                  url,
                  Body.json(model.http.body),
                  model.http.timeout.millis,
                  model.http.credentials,
                  model.http.cache
                ),
                Decoder(
                  Msg.GotHttpResult(_),
                  e => Msg.GotHttpError(e.toString)
                )
              )
            )

      (model, cmd)

    case Msg.GotHttpResult(res) =>
      (model.copy(http = model.http.copy(response = Option(res), error = None)), Cmd.None)

    case Msg.GotHttpError(message) =>
      (model.copy(http = model.http.copy(response = None, error = Option(message))), Cmd.None)

    case Msg.Save(k, v) =>
      val cmd: Cmd[IO, Msg] = LocalStorage.setItem(k, v) { _ =>
        Msg.Log("Save successful")
      }

      (model, cmd)

    case Msg.Load(k) =>
      val cmd: Cmd[IO, Msg] = LocalStorage.getItem(k) {
        case Left(e) => Msg.Log("Error loading: " + e.key)
        case Right(found) =>
          Msg.DataLoaded(found.data)
      }

      (model, cmd)

    case Msg.ClearStorage(k) =>
      val cmd: Cmd[IO, Msg] = LocalStorage.removeItem(k) { _ =>
        Msg.Log("Item removed successfully")
      }

      (model.copy(saveData = None), cmd)

    case Msg.DataLoaded(data) =>
      val cmd = IO("Loaded: " + data).toCmd.map(Msg.Log.apply)
      (model.copy(tmpSaveData = data, saveData = Option(data)), cmd)

    case Msg.StageSaveData(content) =>
      (model.copy(tmpSaveData = content), Cmd.None)

    case Msg.JumpToHomePage =>
      (model.copy(page = Page.Page1), Routing.setLocation(Page.Page1.toUrlPath))

    case Msg.NavigateTo(page) =>
      (model.copy(page = page), Cmd.None)

    case Msg.TakeSnapshot =>
      (model, HotReload.snapshot(hotReloadKey, model, Model.encode))

    case Msg.OverwriteModel(m) =>
      (m, Cmd.None)

    case Msg.Clear =>
      (model.copy(field = ""), Cmd.None)

    case Msg.Log(msg) =>
      (model, Logger.info(msg))

    case Msg.FocusOnInputField =>
      val cmd: Cmd[IO, Msg] = Dom.focus("text-reverse-field") {
        case Left(Dom.NotFound(id)) => Msg.Log("Element not found: " + id)
        case _                      => Msg.Log("Focused on input field")
      }
      (model, cmd)

    case Msg.NewContent(content) =>
      (model.copy(field = content), Cmd.None)

    case Msg.Insert =>
      (model.copy(components = Counter.init :: model.components), Cmd.None)

    case Msg.Remove =>
      val cs = model.components match
        case Nil    => Nil
        case _ :: t => t

      (model.copy(components = cs), Cmd.None)

    case Msg.Modify(id, m) =>
      val cs = model.components.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      (model.copy(components = cs), Cmd.None)

    case Msg.WebSocketStatus(Status.ConnectionError(err)) =>
      val log = IO.println(s"Failed to open WebSocket connection: $err").toCmd
      (model.copy(error = Some(err)), log)

    case Msg.WebSocketStatus(Status.Connected(ws)) =>
      val log = IO.println("WS connected").toCmd
      (model.copy(echoSocket = Some(ws)), log)

    case Msg.WebSocketStatus(Status.Connecting) =>
      val log = IO.println("Establishing WS connection").toCmd
      (
        model,
        Cmd.Batch(
          log,
          WebSocket.connect(
            address = model.socketUrl,
            onOpenMessage = "Connect me!",
            keepAliveSettings = KeepAliveSettings.default
          ) {
            case WebSocketConnect.Error(err) => Status.ConnectionError(err).asMsg
            case WebSocketConnect.Socket(ws) => Status.Connected(ws).asMsg
          }
        )
      )

    case Msg.WebSocketStatus(Status.Disconnecting) =>
      val log = IO.println("Graceful shutdown of WS connection").toCmd
      (
        model.copy(echoSocket = None),
        Cmd.Batch(
          log,
          model.echoSocket.map(_.disconnect).getOrElse(Cmd.None)
        )
      )

    case Msg.WebSocketStatus(Status.Disconnected) =>
      val log = IO.println("WebSocket not connected yet").toCmd
      (model, log)

    case Msg.FromSocket(message) =>
      val log = IO.println("Got: " + message).toCmd
      (model.copy(log = message :: model.log), log)

    case Msg.ToSocket(message) =>
      val log = IO.println("Sent: " + message).toCmd
      (
        model,
        Cmd.Batch(
          log,
          model.echoSocket.map(_.publish(message)).getOrElse(Cmd.None)
        )
      )

    case Msg.NewTime(time) =>
      (model.copy(currentTime = time), Cmd.None)

    case Msg.FrameTick(t) =>
      (model.copy(time = model.time.next(t)), Cmd.None)

  def view(model: Model): Html[Msg] =
    val navItems =
      Page.values.toList.map { pg =>
        if pg == model.page then li(style := CSS.`font-family`("sans-serif"))(pg.toNavLabel)
        else
          li(style := CSS.`font-family`("sans-serif")) {
            a(href := pg.toUrlPath)(pg.toNavLabel)
          }
      } ++
        List(
          li(style := CSS.`font-family`("sans-serif")) {
            a(href := "#foo" + Random.nextInt())("Random link")
          },
          li(style := CSS.`font-family`("sans-serif")) {
            a(href := "https://tyrian.indigoengine.io/")("Tyrian's Website")
          }
        )

    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    val connect =
      if model.echoSocket.isEmpty then div(myStyle)(button(onClick(Status.Connecting.asMsg))(text("Connect")))
      else div(myStyle)(button(onClick(Status.Disconnecting.asMsg))(text("Disconnect")))

    val contents =
      model.page match
        case Page.Page1 =>
          val checkboxes =
            model.fruit.map { fruit =>
              Html.span(
                label(fruit.name),
                input(
                  typ     := "checkbox",
                  checked := fruit.available,
                  onChange(_ => Msg.ToggleFruitAvailability(fruit.name))
                )
              )
            }

          div(onMouseMove(evt => Msg.MouseMove((evt.screenX.toInt, evt.screenY.toInt))))(
            div(
              input(id := "fruitName", onInput(s => Msg.UpdateFruitInput(s))),
              button(onClick(Msg.AddFruit))(
                text("Add Fruit")
              ),
              div(checkboxes)
            ),
            div(id := "mousepos")().innerHtml(s"<p><i>Mouse Coords ${model.mousePosition}</i></p>"),
            label(
              p("Choose an ice cream flavour:"),
              select(cls := "ice-cream", name := "ice-cream", onChange(Msg.NewFlavour(_)))(
                option(value := "")("Select One ..."),
                option(value := "chocolate")("Chocolate"),
                option(value := "sardine")("Sardine"),
                option(value := "vanilla")("Vanilla")
              )
            ),
            p(model.flavour.map(f => s"You like $f").getOrElse("Pick a flavour...")),
            input(
              placeholder := "What should we save?",
              value       := model.tmpSaveData,
              onInput(s => Msg.StageSaveData(s))
            ),
            button(disabled(model.tmpSaveData.isEmpty), onClick(Msg.Save("test-data", model.tmpSaveData)))("Save"),
            button(onClick(Msg.Load("test-data")))("Load"),
            button(onClick(Msg.ClearStorage("test-data")))("Clear"),
            br,
            br,
            button(onClick(Msg.FocusOnInputField))("Focus on the textfield"),
            input(
              id          := "text-reverse-field",
              value       := model.field,
              placeholder := "Text to reverse",
              onInput(s => Msg.NewContent(s)),
              myStyle,
              autofocus
            ),
            div(myStyle)(text(model.field.reverse)),
            button(onClick(Msg.TakeSnapshot))("Snapshot"),
            button(onClick(Msg.Clear))("clear")
          )

        case Page.Page2 =>
          div(elems)

        case Page.Page3 =>
          div(
            connect,
            p(button(onClick(Msg.ToSocket("Hello!")))("send")),
            p("Log:"),
            p(model.log.flatMap(msg => List(text(msg), br)))
          )

        case Page.Page4 =>
          val angle = model.currentTime.getSeconds() * 2 * math.Pi / 60 - math.Pi / 2
          val handX = 50 + 40 * math.cos(angle)
          val handY = 50 + 40 * math.sin(angle)

          def orbit(runningTime: Double, cx: Double, cy: Double, distance: Double): Vector2 =
            val angle = (Math.PI * 2) * (runningTime % 1.0d)
            Vector2(
              (Math.sin(angle) * distance) + cx,
              (Math.cos(angle) * distance) + cy
            )

          val p = orbit(model.time.running, 50, 50, 45)

          svg(viewBox := "0, 0, 100, 100", width := "300px")(
            circle(
              cx   := "50",
              cy   := "50",
              r    := "45",
              fill := "#0B79CE"
            ),
            line(
              x1     := "50",
              y1     := "50",
              x2     := handX.toString,
              y2     := handY.toString,
              stroke := "#023963"
            ),
            circle(
              cx   := p.x.toString,
              cy   := p.y.toString,
              r    := "5",
              fill := "#FF0000"
            )
          )

        case Page.Page5 =>
          val status  = model.http.response.map(r => r.status.toString).getOrElse("..")
          val headers = model.http.response.map(r => r.headers.toList.mkString("[", ", ", "]")).getOrElse("..")
          val body    = model.http.response.map(r => r.body).getOrElse("..")
          val error   = model.http.error.map(e => "Error: " + e).getOrElse("Success!")

          div(
            div(
              text("Method: "),
              select(cls := "http-method", name := "http-method", onChange(Msg.UpdateHttpMethod(_)))(
                option(value := "get")("GET"),
                option(value := "post")("POST"),
                option(value := "put")("PUT"),
                option(value := "delete")("DELETE"),
                option(value := "patch")("PATCH"),
                option(value := "head")("HEAD")
              )
            ),
            div(
              text("URL: "),
              input(
                placeholder := "enter a url",
                value       := model.http.url.getOrElse(""),
                onInput(s => Msg.UpdateHttpDetails(s))
              )
            ),
            div(
              text("Body: "),
              input(
                placeholder := "request body",
                value       := model.http.body,
                disabled(Set(Method.Get, Method.Head).contains(model.http.method)),
                onInput(s => Msg.UpdateHttpBody(s))
              )
            ),
            div(
              text("Timeout: "),
              input(
                placeholder := "timeout",
                value       := model.http.timeout.toString,
                onInput(s => Msg.UpdateHttpTimeout(s))
              )
            ),
            div(
              text("Credentials: "),
              select(cls := "http-credentials", name := "http-credentials", onChange(Msg.UpdateHttpCredentials(_)))(
                option(value := "same-origin")("same-origin"),
                option(value := "omit")("omit"),
                option(value := "include")("include")
              )
            ),
            div(
              div(
                text("Headers: "),
                button(onClick(Msg.UpdateHeaderAdd))(text("+"))
              ),
              div(model.http.headers.zipWithIndex.map { case ((k, v), i) =>
                div(
                  input(
                    placeholder := "key",
                    value       := k,
                    onInput(s => Msg.UpdateHeaderKey(i, s))
                  ),
                  input(
                    placeholder := "value",
                    value       := v,
                    onInput(s => Msg.UpdateHeaderValue(i, s))
                  ),
                  button(onClick(Msg.UpdateHeaderRemove(i)))(text("-"))
                )
              })
            ),
            div(
              text("Cache: "),
              select(cls := "http-cache", name := "http-cache", onChange(Msg.UpdateHttpCache(_)))(
                option(value := "default")("default"),
                option(value := "no-store")("no-store"),
                option(value := "reload")("reload"),
                option(value := "no-cache")("no-cache"),
                option(value := "force-cache")("force-cache"),
                option(value := "only-if-cached")("only-if-cached")
              )
            ),
            p(button(onClick(Msg.MakeHttpRequest))("Fetch!")),
            p("Our server says..."),
            ul(
              li("Status: " + status),
              li("Headers: " + headers),
              li("Body: " + body),
              li(error)
            )
          )

        case Page.Page6 =>
          div(
            form(onSubmit(Msg.Log("submitted")))(
              input(_type := "submit", name := "submit", value := "submit")
            )
          )

    div(
      div(
        h3(style := CSS.`font-family`("sans-serif"))("Navigation:"),
        ol(navItems),
        button(onClick(Msg.JumpToHomePage))("Jump to homepage")
      ),
      div(br),
      contents
    )

  private val myStyle =
    styles(
      "width"      -> "100%",
      "height"     -> "40px",
      "padding"    -> "10px 0",
      "font-size"  -> "2em",
      "text-align" -> "center"
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    val webSocketSubs =
      model.echoSocket.fold(Sub.emit[IO, Msg](Status.Disconnected.asMsg)) {
        _.subscribe {
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
      }

    val stream =
      fs2.Stream
        .awakeEvery[IO](30.seconds)
        .map(t => Msg.Log("30 second pulse at " + t.toString))

    val simpleSubs: Sub[IO, Msg] =
      Sub.timeout[IO, Msg](2.seconds, Msg.Log("Logged this after 2 seconds"), "delayed log") |+|
        Sub.every[IO](1.second, hotReloadKey).map(_ => Msg.TakeSnapshot) |+|
        stream.toSub("pulse")

    val clockSub: Sub[IO, Msg] = Sub.every[IO](1.second, "clock-ticks").map(Msg.NewTime.apply)

    Sub.Batch(
      webSocketSubs,
      simpleSubs,
      clockSub,
      Sub.animationFrameTick("frametick") { t =>
        Msg.FrameTick(t)
      }
    )

enum Msg:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: Counter.Msg)
  case FromSocket(message: String)
  case ToSocket(message: String)
  case FocusOnInputField
  case Log(msg: String)
  case WebSocketStatus(status: Status)
  case Clear
  case StageSaveData(content: String)
  case Save(key: String, value: String)
  case Load(key: String)
  case ClearStorage(key: String)
  case DataLoaded(data: String)
  case NavigateTo(page: Page)
  case JumpToHomePage
  case OverwriteModel(model: Model)
  case TakeSnapshot
  case NewTime(time: js.Date)
  case MakeHttpRequest
  case GotHttpResult(response: Response)
  case GotHttpError(message: String)
  case UpdateHttpDetails(newUrl: String)
  case UpdateHttpBody(body: String)
  case UpdateHttpMethod(method: String)
  case UpdateHttpCredentials(credentials: String)
  case UpdateHttpTimeout(timeout: String)
  case UpdateHeaderKey(headerIndex: Int, key: String)
  case UpdateHeaderValue(headerIndex: Int, value: String)
  case UpdateHeaderAdd
  case UpdateHeaderRemove(headerIndex: Int)
  case UpdateHttpCache(cache: String)
  case FrameTick(runningTime: Double)
  case MouseMove(to: (Int, Int))
  case NewFlavour(name: String)
  case AddFruit
  case UpdateFruitInput(input: String)
  case ToggleFruitAvailability(name: String)

enum Status:
  case Connecting
  case Connected(ws: WebSocket[IO])
  case ConnectionError(msg: String)
  case Disconnecting
  case Disconnected

  def asMsg: Msg = Msg.WebSocketStatus(this)

object Counter:

  opaque type Model = Int

  def init: Model = 0

  enum Msg:
    case Increment, Decrement

  def view(model: Model): Html[Msg] =
    div(
      button(onClick(Msg.Decrement))(text("-")),
      div(text(model.toString)),
      button(onClick(Msg.Increment))(text("+"))
    )

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.Increment => model + 1
      case Msg.Decrement => model - 1

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
    fruitInput: String
)

final case class Fruit(name: String, available: Boolean)

final case class Time(running: Double, delta: Double):
  def next(t: Double): Time =
    this.copy(running = t, delta = t - running)

enum Page:
  case Page1, Page2, Page3, Page4, Page5, Page6

  def toNavLabel: String =
    this match
      case Page1 => "Input fields"
      case Page2 => "Counters"
      case Page3 => "WebSockets"
      case Page4 => "Clock"
      case Page5 => "Http"
      case Page6 => "Form"

  def toUrlPath: String =
    this match
      case Page1 => "/page1"
      case Page2 => "/page2"
      case Page3 => "/page3"
      case Page4 => "/page4"
      case Page5 => "/page5"
      case Page6 => "/page6"

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
      ""
    )

  // We're only saving/loading the input field contents as an example
  def encode(model: Model): String = model.field
  def decode: Option[String] => Either[String, Model] =
    case None => Left("No snapshot found")
    case Some(data) =>
      Right(Model.init.copy(field = data))

final case class HttpDetails(
    method: Method,
    url: Option[String],
    body: String,
    response: Option[Response],
    error: Option[String],
    timeout: Double,
    credentials: RequestCredentials,
    headers: List[(String, String)],
    cache: RequestCache
)
object HttpDetails:
  val initial: HttpDetails =
    HttpDetails(
      Method.Get,
      Option("http://httpbin.org/get"),
      "",
      None,
      None,
      10000,
      RequestCredentials.SameOrigin,
      List(),
      RequestCache.Default
    )

final case class Vector2(x: Double, y: Double)
