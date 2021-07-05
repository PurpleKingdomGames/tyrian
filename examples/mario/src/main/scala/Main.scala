package mario

import org.scalajs.dom.{document, window}
import tyrian.Html._
import tyrian.Sub._
import tyrian._

import cats.syntax.all._

object Main:

  type Model = Mario

  def init: (Model, Cmd[Msg]) =
    (Mario(0, 0, 0, 0, Direction.Right), Cmd.Empty)

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    import Mario._
    msg match
      case Msg.ArrowUpPressed if model.y == 0.0 =>
        val newModel = (jump andThen applyPhysics)(model)
        (newModel, Effects.Cmd.playSound("resources/jump-c-07.mp3"))

      case Msg.ArrowLeftPressed =>
        val newModel = (walkLeft andThen applyPhysics)(model)
        (newModel, Cmd.Empty)

      case Msg.ArrowRightPressed =>
        val newModel = (walkRight andThen applyPhysics)(model)
        (newModel, Cmd.Empty)

      case Msg.ArrowLeftReleased if model.dir == Direction.Left =>
        val newModel = model.copy(vx = 0.0)
        (newModel, Cmd.Empty)

      case Msg.ArrowRightReleased if model.dir == Direction.Right =>
        val newModel = model.copy(vx = 0.0)
        (newModel, Cmd.Empty)

      case Msg.PassageOfTime =>
        (applyPhysics(model), Cmd.Empty)

      case _ =>
        (model, Cmd.Empty)

  def subscriptions(model: Model): Sub[Msg] =
    Effects.keyPressSub(37).map[Msg](_ => Msg.ArrowLeftPressed) <+>
      Effects.keyPressSub(39).map(_ => Msg.ArrowRightPressed) <+>
      Effects.keyReleaseSub(37).map(_ => Msg.ArrowLeftReleased) <+>
      Effects.keyReleaseSub(39).map(_ => Msg.ArrowRightReleased) <+>
      Effects.keyPressSub(38).map(_ => Msg.ArrowUpPressed) <+>
      Effects.requestAnimationFrameSub.map(_ => Msg.PassageOfTime) <+>
      Effects.touchPressedSub(model) <+>
      Effects.touchReleasedSub(model)

  def view(model: Model): Html[Msg] =
    val (posX, posY) =
      modelPositionScreen(window.innerWidth, window.innerHeight, model)

    val verb = (model.y > 0, model.vx != 0) match {
      case (true, _) => "jump"
      case (_, true) => "walk"
      case _         => "stand"
    }

    val dir = model.dir.toString.toLowerCase
    val css = Style("top", s"${posY}px") |+| Style("left", s"${posX}px")

    div(List(style(css)) ++ attributes("id" -> "mario", "class" -> s"character $verb $dir"))()

  def modelPositionScreen(screenX: Double, screenY: Double, model: Model): (Double, Double) =
    val posX = ((screenX / 2) * 100) / 300 + model.x
    val posY = ((screenY - 200) * 100) / 300 - model.y
    (posX, posY)

  def main(args: Array[String]): Unit =
    Tyrian.start(document.querySelector("#mario"), init, update, view, subscriptions)

end Main

enum Msg:
  case PassageOfTime, ArrowLeftPressed, ArrowRightPressed, ArrowLeftReleased, ArrowRightReleased, ArrowUpPressed

enum Direction:
  case Left, Right

final case class Mario(x: Double, y: Double, vx: Double, vy: Double, dir: Direction)
object Mario:
  val gravity                      = 0.25
  val applyGravity: Mario => Mario = mario => mario.copy(vy = if (mario.y > 0) mario.vy - gravity else 0)
  val applyMotion: Mario => Mario  = mario => mario.copy(x = mario.x + mario.vx, y = Math.max(0.0, mario.y + 3 * mario.vy))
  val walkLeft: Mario => Mario     = _.copy(vx = -1.5, dir = Direction.Left)
  val walkRight: Mario => Mario    = _.copy(vx = 1.5, dir = Direction.Right)
  val jump: Mario => Mario         = _.copy(vy = 4.0)
  val applyPhysics: Mario => Mario = applyMotion andThen applyGravity
