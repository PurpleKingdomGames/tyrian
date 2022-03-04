package example

import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Dom
import tyrian.cmds.LocalStorage
import tyrian.cmds.Logger
import tyrian.websocket.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Sandbox extends TyrianApp[Msg, Model]:

  val hotReloadKey: String = "hotreload"

  def init(flags: Map[String, String]): (Model, Cmd[Msg]) =
    val cmds =
      Cmd.Batch(
        HotReload.bootstrap(hotReloadKey, Model.decode) {
          case Left(e)      => Msg.Log("Error during hot-reload!: " + e.message)
          case Right(model) => Msg.OverwriteModel(model)
        },
        Logger.info(flags.toString),
        Navigation.getLocationHash {
          case Left(Navigation.NoHash)             => Msg.NavigateTo(Page.Page1)
          case Right(Navigation.CurrentHash(hash)) => Msg.NavigateTo(Page.fromString(hash))
        }
      )

    (Model.init, cmds)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.Save(k, v) =>
        val cmd = LocalStorage.setItem(k, v) {
          case Left(e)  => Msg.Log("Error saving: " + e)
          case Right(_) => Msg.Log("Save successful")
        }

        (model, cmd)

      case Msg.Load(k) =>
        val cmd = LocalStorage.getItem(k) {
          case Left(e) => Msg.Log("Error loading: " + e)
          case Right(data) =>
            println("Loaded: " + data)
            Msg.DataLoaded(data)
        }

        (model, cmd)

      case Msg.ClearStorage(k) =>
        val cmd = LocalStorage.removeItem(k) {
          case Left(e)     => Msg.Log("Error removing: " + e)
          case Right(data) => Msg.Log("Item remove successful")
        }

        (model.copy(saveData = None), cmd)

      case Msg.DataLoaded(data) =>
        (model.copy(tmpSaveData = data, saveData = Option(data)), Cmd.Empty)

      case Msg.StageSaveData(content) =>
        (model.copy(tmpSaveData = content), Cmd.Empty)

      case Msg.JumpToHomePage =>
        (model, Navigation.setLocationHash(Page.Page1.toHash))

      case Msg.NavigateTo(page) =>
        (model.copy(page = page), Cmd.Empty)

      case Msg.TakeSnapshot =>
        (model, HotReload.snapshot(hotReloadKey, model, Model.encode))

      case Msg.OverwriteModel(m) =>
        (m, Cmd.Empty)

      case Msg.Clear =>
        (model.copy(field = ""), Cmd.Empty)

      case Msg.Log(msg) =>
        (model, Logger.info(msg))

      case Msg.FocusOnInputField =>
        val cmd = Dom.focus("text-reverse-field") {
          case Left(Dom.NotFound(id)) => Msg.Log("Element not found: " + id)
          case _                      => Msg.Log("Focused on input field")
        }
        (model, cmd)

      case Msg.NewContent(content) =>
        (model.copy(field = content), Cmd.Empty)

      case Msg.Insert =>
        (model.copy(components = Counter.init :: model.components), Cmd.Empty)

      case Msg.Remove =>
        val cs = model.components match
          case Nil    => Nil
          case _ :: t => t

        (model.copy(components = cs), Cmd.Empty)

      case Msg.Modify(id, m) =>
        val cs = model.components.zipWithIndex.map { case (c, i) =>
          if i == id then Counter.update(m, c) else c
        }

        (model.copy(components = cs), Cmd.Empty)

      case Msg.WebSocketStatus(Status.ConnectionError(err)) =>
        println(s"Failed to open WebSocket connection: $err")
        (model.copy(error = Some(err)), Cmd.Empty)

      case Msg.WebSocketStatus(Status.Connected(ws)) =>
        (model.copy(echoSocket = Some(ws)), Cmd.Empty)

      case Msg.WebSocketStatus(Status.Connecting) =>
        (
          model,
          WebSocket.connect(
            address = model.socketUrl,
            onOpenMessage = "Connect me!",
            keepAliveSettings = KeepAliveSettings.default
          ) {
            case Left(err) => Status.ConnectionError(err).asMsg
            case Right(ws) => Status.Connected(ws).asMsg
          }
        )

      case Msg.WebSocketStatus(Status.Disconnecting) =>
        println("Graceful shutdown of WS connection")
        (model.copy(echoSocket = None), model.echoSocket.map(_.disconnect).getOrElse(Cmd.Empty))

      case Msg.WebSocketStatus(Status.Disconnected) =>
        println("WebSocket not connected yet")
        (model, Cmd.Empty)

      case Msg.FromSocket(message) =>
        println("Got: " + message)
        (model.copy(log = message :: model.log), Cmd.Empty)

      case Msg.ToSocket(message) =>
        println("Sent: " + message)
        (model, model.echoSocket.map(_.publish(message)).getOrElse(Cmd.Empty))

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

  def subscriptions(model: Model): Sub[Msg] =
    val webSocketSubs =
      model.echoSocket.fold(Sub.emit(Status.Disconnected.asMsg)) {
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

    Sub.Batch(
      webSocketSubs,
      Navigation.onLocationHashChange(hashChange => Msg.NavigateTo(Page.fromString(hashChange.newFragment))),
      Sub.every(1.second, hotReloadKey).map(_ => Msg.TakeSnapshot)
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

enum Status:
  case Connecting
  case Connected(ws: WebSocket)
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
    echoSocket: Option[WebSocket],
    socketUrl: String,
    field: String,
    components: List[Counter.Model],
    log: List[String],
    error: Option[String],
    tmpSaveData: String,
    saveData: Option[String]
)

enum Page:
  case Page1, Page2, Page3

  def toNavLabel: String =
    this match
      case Page1 => "Input fields"
      case Page2 => "Counters"
      case Page3 => "WebSockets"

  def toHash: String =
    this match
      case Page1 => "#page1"
      case Page2 => "#page2"
      case Page3 => "#page3"

object Page:
  def fromString(pageString: String): Page =
    pageString match
      case "#page2" => Page2
      case "page2"  => Page2
      case "#page3" => Page3
      case "page3"  => Page3
      case s        => Page1

object Model:
  // val echoServer = "ws://ws.ifelse.io" // public echo server
  val echoServer = "ws://localhost:8080/wsecho"

  val init: Model =
    Model(Page.Page1, None, echoServer, "", Nil, Nil, None, "", None)

  // We're only saving/loading the input field contents as an example
  def encode(model: Model): String = model.field
  def decode: Option[String] => Model =
    case None       => Model.init
    case Some(data) => Model(Page.Page1, None, echoServer, data, Nil, Nil, None, "", None)
