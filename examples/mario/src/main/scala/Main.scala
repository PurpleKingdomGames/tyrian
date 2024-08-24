package mario

import cats.effect.IO
import org.scalajs.dom.window
import tyrian.Html.*
import tyrian.Sub.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Mario]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Mario, Cmd[IO, Msg]) =
    (Mario(0, 0, 0, 0, Direction.Right), Cmd.None)

  def update(model: Mario): Msg => (Mario, Cmd[IO, Msg]) =
    case Msg.ArrowUpPressed if model.y == 0.0 =>
      val newModel = (Mario.jump andThen Mario.applyPhysics)(model)
      (newModel, Effects.playSound("resources/jump-c-07.mp3"))

    case Msg.ArrowLeftPressed =>
      val newModel = (Mario.walkLeft andThen Mario.applyPhysics)(model)
      (newModel, Cmd.None)

    case Msg.ArrowRightPressed =>
      val newModel = (Mario.walkRight andThen Mario.applyPhysics)(model)
      (newModel, Cmd.None)

    case Msg.ArrowLeftReleased if model.dir == Direction.Left =>
      val newModel = model.copy(vx = 0.0)
      (newModel, Cmd.None)

    case Msg.ArrowRightReleased if model.dir == Direction.Right =>
      val newModel = model.copy(vx = 0.0)
      (newModel, Cmd.None)

    case Msg.PassageOfTime =>
      (Mario.applyPhysics(model), Cmd.None)

    case Msg.NoOp =>
      (model, Cmd.None)

    case _ =>
      (model, Cmd.None)

  def subscriptions(model: Mario): Sub[IO, Msg] =
    Sub.Batch(
      Effects.keyPressSub(37).map[Msg](_ => Msg.ArrowLeftPressed),
      Effects.keyPressSub(39).map(_ => Msg.ArrowRightPressed),
      Effects.keyReleaseSub(37).map(_ => Msg.ArrowLeftReleased),
      Effects.keyReleaseSub(39).map(_ => Msg.ArrowRightReleased),
      Effects.keyPressSub(38).map(_ => Msg.ArrowUpPressed),
      Effects.requestAnimationFrameSub.map(_ => Msg.PassageOfTime),
      Effects.touchPressedSub(model),
      Effects.touchReleasedSub(model)
    )

  def view(model: Mario): Html[Msg] =
    val (posX, posY) =
      modelPositionScreen(window.innerWidth, window.innerHeight, model)

    val verb = (model.y > 0, model.vx != 0) match {
      case (true, _) => "jump"
      case (_, true) => "walk"
      case _         => "stand"
    }

    val dir = model.dir.toString.toLowerCase
    val css = Style("top", s"${posY}px") |+| Style("left", s"${posX}px")

    div(
      List(style(css)) ++ attributes(
        "id"    -> "mario",
        "class" -> s"character $verb $dir"
      )
    )()

  def modelPositionScreen(
      screenX: Double,
      screenY: Double,
      model: Mario
  ): (Double, Double) =
    val posX = ((screenX / 2) * 100) / 300 + model.x
    val posY = ((screenY - 200) * 100) / 300 - model.y
    (posX, posY)

end Main

enum Msg:
  case PassageOfTime, ArrowLeftPressed, ArrowRightPressed, ArrowLeftReleased,
    ArrowRightReleased, ArrowUpPressed, NoOp

enum Direction:
  case Left, Right

final case class Mario(
    x: Double,
    y: Double,
    vx: Double,
    vy: Double,
    dir: Direction
)
object Mario:
  val gravity                      = 0.25
  val applyGravity: Mario => Mario = mario => mario.copy(vy = if (mario.y > 0) mario.vy - gravity else 0)
  val applyMotion: Mario => Mario = mario =>
    mario.copy(
      x = mario.x + mario.vx,
      y = Math.max(0.0, mario.y + 3 * mario.vy)
    )
  val walkLeft: Mario => Mario     = _.copy(vx = -1.5, dir = Direction.Left)
  val walkRight: Mario => Mario    = _.copy(vx = 1.5, dir = Direction.Right)
  val jump: Mario => Mario         = _.copy(vy = 4.0)
  val applyPhysics: Mario => Mario = applyMotion andThen applyGravity
