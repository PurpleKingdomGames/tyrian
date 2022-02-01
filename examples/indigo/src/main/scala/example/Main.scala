package example

import example.game.MyAwesomeGame
import org.scalajs.dom.document
import tyrian.Html._
import tyrian._
import tyrian.cmds.Logger

object Main extends TyrianIndigoBridge[String]:

  val gameDivId1: String = "my-game-1"
  val gameDivId2: String = "my-game-2"

  enum Msg:
    case NewContent(content: String) extends Msg
    case StartIndigo                 extends Msg
    case IndigoReceive(msg: String)  extends Msg

  def init: (Model, Cmd[Msg]) =
    (Model.init, Cmd.Emit(Msg.StartIndigo))

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match
      case Msg.NewContent(content) =>
        val cmds =
          Cmd.Batch(
            bridge.sendTo(IndigoGameId(gameDivId1), content),
            bridge.sendTo(IndigoGameId(gameDivId2), content)
          )
        (model.copy(field = content), cmds)

      case Msg.StartIndigo =>
        (
          model,
          Cmd.Batch(
            Cmd.SideEffect { () =>
              MyAwesomeGame(bridge.subSystemFor(IndigoGameId(gameDivId1)), true)
                .launch(
                  gameDivId1,
                  "width"  -> "200",
                  "height" -> "200"
                )
            },
            Cmd.SideEffect { () =>
              MyAwesomeGame(
                bridge.subSystemFor(IndigoGameId(gameDivId2)),
                false
              )
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

  def subscriptions(model: Model): Sub[Msg] =
    Sub.Batch(
      bridge.subscribe { case msg =>
        Some(Msg.IndigoReceive(s"[Any game!] ${msg}"))
      },
      bridge.subscribeTo(IndigoGameId(gameDivId1)) { case msg =>
        Some(Msg.IndigoReceive(s"[$gameDivId1] ${msg}"))
      },
      bridge.subscribeTo(IndigoGameId(gameDivId2)) { case msg =>
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

  def main(args: Array[String]): Unit =
    Tyrian.start(
      document.getElementById("myapp"),
      init,
      update,
      view,
      subscriptions
    )

final case class Model(field: String)
object Model:
  val init: Model =
    Model("")
