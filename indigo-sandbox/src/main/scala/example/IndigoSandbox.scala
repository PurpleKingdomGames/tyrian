package example

import cats.effect.IO
import example.game.MyAwesomeGame
import org.scalajs.dom.document
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger
import tyrian.cmds.Random

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object IndigoSandbox extends TyrianApp[Msg, Model]:

  val gameDivId1: String    = "my-game-1"
  val gameDivId2: String    = "my-game-2"
  val gameId1: IndigoGameId = IndigoGameId("reverse")
  val gameId2: IndigoGameId = IndigoGameId("combine")

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.Emit(Msg.StartIndigo))

  def update(msg: Msg, model: Model): (Model, Cmd[IO, Msg]) =
    msg match
      case Msg.NewRandomInt(i) =>
        (model.copy(randomNumber = i), Cmd.Empty)

      case Msg.NewContent(content) =>
        val cmds =
          Cmd.Batch[IO, Msg](
            model.bridge.publish(gameId1, content),
            model.bridge.publish(gameId2, content),
            Random.int[IO].map(next => Msg.NewRandomInt(next.value))
          )
        (model.copy(field = content), cmds)

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

      case Msg.StartIndigo =>
        (
          model,
          Cmd.Batch(
            Cmd.SideEffect {
              MyAwesomeGame(model.bridge.subSystem(gameId1), true)
                .launch(
                  gameDivId1,
                  "width"  -> "200",
                  "height" -> "200"
                )
            },
            Cmd.SideEffect {
              MyAwesomeGame(model.bridge.subSystem(gameId2), false)
                .launch(
                  gameDivId2,
                  "width"  -> "200",
                  "height" -> "200"
                )
            }
          )
        )

      case Msg.IndigoReceive(msg) =>
        (model, Logger.consoleLog("(Tyrian) from indigo: " + msg))

  def view(model: Model): Html[Msg] =
    val counters = model.components.zipWithIndex.map { case (c, i) =>
      Counter.view(c).map(msg => Msg.Modify(i, msg))
    }

    val elems = List(
      button(onClick(Msg.Remove))(text("remove")),
      button(onClick(Msg.Insert))(text("insert"))
    ) ++ counters

    div(
      div(hidden(false))("Random number: " + model.randomNumber.toString),
      div(id := gameDivId1)(),
      div(id := gameDivId2)(),
      div(
        input(placeholder := "Text to reverse", onInput(s => Msg.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems)
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.Batch(
      model.bridge.subscribe { case msg =>
        Some(Msg.IndigoReceive(s"[Any game!] ${msg}"))
      },
      model.bridge.subscribe(gameId1) { case msg =>
        Some(Msg.IndigoReceive(s"[${gameId1.toString}] ${msg}"))
      },
      model.bridge.subscribe(gameId2) { case msg =>
        Some(Msg.IndigoReceive(s"[${gameId2.toString}] ${msg}"))
      }
    )

  private val myStyle =
    styles(
      CSS.width("100%"),
      CSS.height("40px"),
      CSS.padding("10px 0"),
      CSS.`font-size`("2em"),
      CSS.`text-align`("center")
    )

enum Msg:
  case NewContent(content: String)      extends Msg
  case Insert                           extends Msg
  case Remove                           extends Msg
  case Modify(i: Int, msg: Counter.Msg) extends Msg
  case StartIndigo                      extends Msg
  case IndigoReceive(msg: String)       extends Msg
  case NewRandomInt(i: Int)             extends Msg

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
    bridge: TyrianIndigoBridge[IO, String],
    field: String,
    components: List[Counter.Model],
    randomNumber: Int
)
object Model:
  val init: Model =
    Model(TyrianIndigoBridge(), "", Nil, 0)
