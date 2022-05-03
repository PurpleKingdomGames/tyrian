package example

import cats.effect.IO
import tyrian.Html.*
import tyrian.SVG.*
import tyrian.*
import tyrian.cmds.Dom
import tyrian.cmds.LocalStorage
import tyrian.cmds.Logger
import tyrian.websocket.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

import scalajs.js

@JSExportTopLevel("TyrianApp")
object Sandbox extends TyrianApp[Msg, Model]:

  val hotReloadKey: String = "hotreload"

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    val cmds: Cmd[IO, Msg] =
      Cmd.Batch(
        HotReload.bootstrap(hotReloadKey, Model.decode) {
          case Left(msg)    => Msg.Log("Error during hot-reload!: " + msg)
          case Right(model) => Msg.OverwriteModel(model)
        },
        Logger.info(flags.toString),
        Navigation.getLocationHash {
          case Navigation.Result.CurrentHash(hash) => Msg.NavigateTo(Page.fromString(hash))
          case _                                   => Msg.NavigateTo(Page.Page1)
        },
        LocalStorage.key(0) {
          case LocalStorage.Result.Key(key) => Msg.Log("Found local storage key: " + key)
          case _                            => Msg.Log("No local storage enties found.")
        },
        LocalStorage.length(l => Msg.Log("Number of local storage entries: " + l.length))
      )

    (Model.init, cmds)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Save(k, v) =>
      val cmd: Cmd[IO, Msg] = LocalStorage.setItem(k, v) { _ =>
        Msg.Log("Save successful")
      }

      (model, cmd)

    case Msg.Load(k) =>
      val cmd: Cmd[IO, Msg] = LocalStorage.getItem(k) {
        case Left(e) => Msg.Log("Error loading: " + e.key)
        case Right(found) =>
          println("Loaded: " + found.data)
          Msg.DataLoaded(found.data)
      }

      (model, cmd)

    case Msg.ClearStorage(k) =>
      val cmd: Cmd[IO, Msg] = LocalStorage.removeItem(k) { _ =>
        Msg.Log("Item removed successfully")
      }

      (model.copy(saveData = None), cmd)

    case Msg.DataLoaded(data) =>
      (model.copy(tmpSaveData = data, saveData = Option(data)), Cmd.None)

    case Msg.StageSaveData(content) =>
      (model.copy(tmpSaveData = content), Cmd.None)

    case Msg.JumpToHomePage =>
      (model, Navigation.setLocationHash(Page.Page1.toHash))

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
      println(s"Failed to open WebSocket connection: $err")
      (model.copy(error = Some(err)), Cmd.None)

    case Msg.WebSocketStatus(Status.Connected(ws)) =>
      println("WS connected")
      (model.copy(echoSocket = Some(ws)), Cmd.None)

    case Msg.WebSocketStatus(Status.Connecting) =>
      println("Establishing WS connection")
      (
        model,
        WebSocket.connect(
          address = model.socketUrl,
          onOpenMessage = "Connect me!",
          keepAliveSettings = KeepAliveSettings.default
        ) {
          case WebSocketConnect.Error(err) => Status.ConnectionError(err).asMsg
          case WebSocketConnect.Socket(ws) => Status.Connected(ws).asMsg
        }
      )

    case Msg.WebSocketStatus(Status.Disconnecting) =>
      println("Graceful shutdown of WS connection")
      (model.copy(echoSocket = None), model.echoSocket.map(_.disconnect).getOrElse(Cmd.None))

    case Msg.WebSocketStatus(Status.Disconnected) =>
      println("WebSocket not connected yet")
      (model, Cmd.None)

    case Msg.FromSocket(message) =>
      println("Got: " + message)
      (model.copy(log = message :: model.log), Cmd.None)

    case Msg.ToSocket(message) =>
      println("Sent: " + message)
      (model, model.echoSocket.map(_.publish(message)).getOrElse(Cmd.None))
    
    case Msg.NewTime(time) => 
      (model.copy(currentTime = time), Cmd.None)

  def view(model: Model): Html[Msg] =
    val navItems =
      Page.values.toList.map { pg =>
        if pg == model.page then li(pg.toNavLabel)
        else li(a(href := pg.toHash)(pg.toNavLabel))
      }

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
          div(
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
              myStyle
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
            )
          )

    div(
      div(
        h3("Navigation:"),
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

    val simpleSubs: Sub[IO, Msg] =
      Sub.timeout[IO, Msg](2.seconds, Msg.Log("Logged this after 2 seconds"), "delayed log") |+|
        Sub.every[IO](1.second, hotReloadKey).map(_ => Msg.TakeSnapshot)

    val clockSub: Sub[IO, Msg] = Sub.every[IO](1.second, "clock-ticks").map(Msg.NewTime.apply)

    Sub.Batch(
      webSocketSubs,
      Navigation.onLocationHashChange(hashChange => Msg.NavigateTo(Page.fromString(hashChange.newFragment))),
      simpleSubs,
      clockSub
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
    currentTime: js.Date
)

enum Page:
  case Page1, Page2, Page3, Page4

  def toNavLabel: String =
    this match
      case Page1 => "Input fields"
      case Page2 => "Counters"
      case Page3 => "WebSockets"
      case Page4 => "Clock"

  def toHash: String =
    this match
      case Page1 => "#page1"
      case Page2 => "#page2"
      case Page3 => "#page3"
      case Page4 => "#page4"

object Page:
  def fromString(pageString: String): Page =
    pageString match
      case "#page2" => Page2
      case "page2"  => Page2
      case "#page3" => Page3
      case "page3"  => Page3
      case "#page4" => Page4
      case "page4"  => Page4
      case s        => Page1

object Model:
  // val echoServer = "ws://ws.ifelse.io" // public echo server
  val echoServer = "ws://localhost:8080/wsecho"

  val init: Model =
    Model(Page.Page1, None, echoServer, "", Nil, Nil, None, "", None, new js.Date())

  // We're only saving/loading the input field contents as an example
  def encode(model: Model): String = model.field
  def decode: Option[String] => Either[String, Model] =
    case None       => Left("No snapshot found")
    case Some(data) => Right(Model(Page.Page1, None, echoServer, data, Nil, Nil, None, "", None, new js.Date()))
