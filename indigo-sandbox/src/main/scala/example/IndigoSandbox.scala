package example

import tyrian._
import tyrian.Html._
import org.scalajs.dom.document
import example.game.MyAwesomeGame
import tyrian.cmds.Logger

object IndigoSandbox extends TyrianIndigoBridge[String]:

  val gameDivId1: String = "my-game-1"
  val gameDivId2: String = "my-game-2"

  enum Msg:
    case NewContent(content: String)      extends Msg
    case Insert                           extends Msg
    case Remove                           extends Msg
    case Modify(i: Int, msg: Counter.Msg) extends Msg
    case StartIndigo                      extends Msg
    case IndigoReceive(msg: String)       extends Msg

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
            Cmd.SideEffect { () =>
              MyAwesomeGame(bridge.subSystemFor(IndigoGameId(gameDivId1)), true)
                .launch(
                  gameDivId1,
                  "width"  -> "200",
                  "height" -> "200"
                )
            },
            Cmd.SideEffect { () =>
              MyAwesomeGame(bridge.subSystemFor(IndigoGameId(gameDivId2)), false)
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
      div(id(gameDivId1))(),
      div(id(gameDivId2))(),
      div(
        input(placeholder("Text to reverse"), onInput(s => Msg.NewContent(s)), myStyle),
        div(myStyle)(text(model.field.reverse))
      ),
      div(elems)
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
    Tyrian.start(document.getElementById("myapp"), init, update, view, subscriptions)

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

final case class Model(field: String, components: List[Counter.Model])
object Model:
  val init: Model =
    Model("", Nil)
