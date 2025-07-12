package example

import tyrian.*
import tyrian.Html.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object SandboxIORemix extends TyrianIO[Model]:

  def router: Location => GlobalMsg = Routing.externalOnly(AppEvent.NoOp, AppEvent.FollowLink(_))

  def init(flags: Map[String, String]): Outcome[Model] =
    Outcome(Model.init)

  def update(model: Model): GlobalMsg => Outcome[Model] =
    case e: AppEvent =>
      handleAppEvent(model)(e)

    case _ =>
      Outcome(model)

  def handleAppEvent(model: Model): AppEvent => Outcome[Model] =
    case AppEvent.NoOp =>
      Outcome(model)

    case AppEvent.FollowLink(href) =>
      Outcome(model)
        .addActions(
          Action.fromCmd(Nav.loadUrl(href))
        ) // TODO: Will need Nav equivalents. OR, we just accept Cmds in the addActions, or make a new addCmds thing.

    case AppEvent.NewRandomInt(i) =>
      Outcome(model.copy(randomNumber = i))

    case AppEvent.NewContent(content) =>
      // val actions = // TODO: Bring back, might need to repackage the originals...
      //   Logger.info[Task]("New content: " + content) |+|
      //     Random.int[Task].map(next => GlobalMsg.NewRandomInt(next.value))

      Outcome(model.copy(field = content))
      // .addActions(actions)

    case AppEvent.Insert =>
      Outcome(
        model.copy(
          components = Counter.init :: model.components
        )
      )

    case AppEvent.Remove =>
      val cs = model.components match
        case Nil    => Nil
        case _ :: t => t

      Outcome(model.copy(components = cs))

    case AppEvent.Modify(id, m) =>
      val cs = model.components.zipWithIndex.map { case (c, i) =>
        if i == id then Counter.update(m, c) else c
      }

      Outcome(model.copy(components = cs))

  def view(model: Model): Html[GlobalMsg] =
    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => AppEvent.Modify(i, msg))
    }

    val elems = List(
      button(onClick(AppEvent.Remove))(text("remove")),
      button(onClick(AppEvent.Insert))(text("insert"))
    ) ++ counters

    div(
      div(hidden(false))("Random number: " + model.randomNumber.toString),
      div(
        a(href := "/another-page")("Internal link (will be ignored)"),
        br,
        a(href := "http://tyrian.indigoengine.io/")("Tyrian website")
      ),
      div(
        input(placeholder := "Text to reverse", onInput(s => AppEvent.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems)
    )

  def watchers(model: Model): Watch =
    Watch.None

  private val myStyle =
    styles(
      CSS.width("100%"),
      CSS.height("40px"),
      CSS.padding("10px 0"),
      CSS.`font-size`("2em"),
      CSS.`text-align`("center")
    )

enum AppEvent extends GlobalMsg:
  case NewContent(content: String)
  case Insert
  case Remove
  case Modify(i: Int, msg: CounterEvent)
  case NewRandomInt(i: Int)
  case FollowLink(href: String)
  case NoOp

// TODO: This needs reworking. Should be a CounterManager that delegates and handles it's own messages and so on.
// TODO: Think about 'out of order' rendering: HtmlFragment?
object Counter:

  opaque type Model = Int

  def init: Model = 0

  def view(model: Model): Html[CounterEvent] =
    div(
      button(onClick(CounterEvent.Decrement))(text("-")),
      div(text(model.toString)),
      button(onClick(CounterEvent.Increment))(text("+"))
    )

  def update(msg: CounterEvent, model: Model): Model =
    msg match
      case CounterEvent.Increment => model + 1
      case CounterEvent.Decrement => model - 1

enum CounterEvent extends GlobalMsg:
  case Increment, Decrement

final case class Model(
    field: String,
    components: List[Counter.Model],
    randomNumber: Int
)
object Model:
  val init: Model =
    Model("", Nil, 0)
