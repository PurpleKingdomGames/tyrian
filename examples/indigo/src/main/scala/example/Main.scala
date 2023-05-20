package example

import cats.effect.IO
import example.game.MyAwesomeGame
import org.scalajs.dom.document
import tyrian.Html.*
import tyrian.*
import tyrian.cmds.Logger

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  val gameDivId1: String    = "my-game-1"
  val gameDivId2: String    = "my-game-2"
  val gameId1: IndigoGameId = IndigoGameId("reverse")
  val gameId2: IndigoGameId = IndigoGameId("combine")

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.Emit(Msg.StartIndigo))

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.NewContent(content) =>
      val cmds =
        Cmd.Batch(
          model.bridge.publish(gameId1, content),
          model.bridge.publish(gameId2, content)
        )
      (model.copy(field = content), cmds)

    case Msg.StartIndigo =>
      val task: IO[Msg] =
        IO {
          if gameDivsExist(gameDivId1, gameDivId2) then
            println("Indigo container divs ready, launching games.")
            MyAwesomeGame(model.bridge.subSystem(gameId1), true)
              .launch(
                gameDivId1,
                "width"  -> "200",
                "height" -> "200"
              )
            MyAwesomeGame(model.bridge.subSystem(gameId2), false)
              .launch(
                gameDivId2,
                "width"  -> "200",
                "height" -> "200"
              )

            Msg.NoOp
          else
            println("Indigo container divs not ready, retrying...")
            Msg.RetryIndigo
        }

      (model, Cmd.Run(task))

    case Msg.RetryIndigo =>
      (model, Cmd.emitAfterDelay(Msg.StartIndigo, 0.5.seconds))

    case Msg.IndigoReceive(msg) =>
      (model, Logger.consoleLog("(Tyrian) from indigo: " + msg))

    case Msg.NoOp =>
      (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div(
      div(id := gameDivId1)(),
      div(id := gameDivId2)(),
      div(
        p(b("Enter text below and check the browser console for output.")),
        p(
          "The text you add is sent to both running Indigo games, one send the message back reversed and the other sends it back doubled up with a '_' separator."
        ),
        input(
          placeholder := "Text to reverse",
          onInput(s => Msg.NewContent(s)),
          myStyle
        ),
        div(myStyle)(text(model.field.reverse))
      )
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.Batch(
      model.bridge.subscribe { case msg =>
        Some(Msg.IndigoReceive(s"[Any game!] ${msg}"))
      },
      model.bridge.subscribe(gameId1) { case msg =>
        Some(Msg.IndigoReceive(s"[$gameDivId1] ${msg}"))
      },
      model.bridge.subscribe(gameId2) { case msg =>
        Some(Msg.IndigoReceive(s"[$gameDivId2] ${msg}"))
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

  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  private def gameDivsExist(id1: String, id2: String): Boolean =
    document.getElementById(id1) != null &&
      document.getElementById(id2) != null

enum Msg:
  case NewContent(content: String)
  case StartIndigo
  case RetryIndigo
  case IndigoReceive(msg: String)
  case NoOp

final case class Model(bridge: TyrianIndigoBridge[IO, String], field: String)
object Model:
  val init: Model =
    Model(TyrianIndigoBridge(), "")
