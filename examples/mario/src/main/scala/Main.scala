package mario

import org.scalajs.dom.{document, window}
import scalm.Html._
import scalm.Sub._
import scalm._

import scala.math._
import cats.syntax.all._

object Main extends App {

  def main(args: Array[String]): Unit =
    Scalm.start(this, document.querySelector("#mario"))

  sealed trait Direction
  case object Left extends Direction
  case object Right extends Direction

  case class Mario(x: Double, y: Double, vx: Double, vy: Double, dir: Direction)

  type Model = Mario

  def init: (Model, Cmd[Msg]) = (Mario(0, 0, 0, 0, Right), Cmd.Empty)

  sealed trait Msg
  case object PassageOfTime extends Msg
  case object ArrowLeftPressed extends Msg
  case object ArrowRightPressed extends Msg
  case object ArrowLeftReleased extends Msg
  case object ArrowRightReleased extends Msg
  case object ArrowUpPressed extends Msg

  val gravity = 0.25
  val applyGravity: Mario => Mario = (mario) =>
    mario.copy(vy = if (mario.y > 0) mario.vy - gravity else 0)

  val applyMotion: Mario => Mario = (mario: Model) =>
    mario.copy(x = mario.x + mario.vx, y = max(0.0, mario.y + 3 * mario.vy))

  val walkLeft: Model => Model = _.copy(vx = -1.5, dir = Left)

  val walkRight: Model => Model = _.copy(vx = 1.5, dir = Right)

  val jump: Model => Model = _.copy(vy = 4.0)

  val applyPhysics: Model => Model = applyGravity compose applyMotion

  def update(msg: Msg, model: Model): (Model, Cmd[Msg]) =
    msg match {
      case ArrowUpPressed if model.y == 0.0 =>
        val newModel = (jump andThen applyPhysics)(model)
        (newModel, Effects.Cmd.playSound("resources/jump-c-07.mp3"))

      case ArrowLeftPressed =>
        val newModel = (walkLeft andThen applyPhysics)(model)
        (newModel, Cmd.Empty)

      case ArrowRightPressed =>
        val newModel = (walkRight andThen applyPhysics)(model)
        (newModel, Cmd.Empty)

      case ArrowLeftReleased if model.dir == Left =>
        val newModel = model.copy(vx = 0.0)
        (newModel, Cmd.Empty)

      case ArrowRightReleased if model.dir == Right =>
        val newModel = model.copy(vx = 0.0)
        (newModel, Cmd.Empty)

      case PassageOfTime => (applyPhysics(model), Cmd.Empty)
      case _             => (model, Cmd.Empty)
    }

  def subscriptions(model: Model): Sub[Msg] =
    Effects.keyPressSub(37).map[Msg](_ => ArrowLeftPressed) <+>
      Effects.keyPressSub(39).map(_ => ArrowRightPressed) <+>
      Effects.keyReleaseSub(37).map(_ => ArrowLeftReleased) <+>
      Effects.keyReleaseSub(39).map(_ => ArrowRightReleased) <+>
      Effects.keyPressSub(38).map(_ => ArrowUpPressed) <+>
      Effects.requestAnimationFrameSub.map(_ => PassageOfTime) <+>
      Effects.touchPressedSub(model) <+>
      Effects.touchReleasedSub(model)

  def view(model: Model): Html[Msg] = {

    val (posX, posY) =
      modelPositionScreen(window.innerWidth, window.innerHeight, model)

    val verb = (model.y > 0, model.vx != 0) match {
      case (true, _) => "jump"
      case (_, true) => "walk"
      case _         => "stand"
    }

    val dir = model.dir.toString.toLowerCase
    val css = Style("top", s"${posY}px") |+| Style("left", s"${posX}px")

    div(style(css),
        attr("id", "mario"),
        attr("class", "character " + verb + " " + dir))()
  }

  def modelPositionScreen(screenX: Double,
                          screenY: Double,
                          model: Model): (Double, Double) = {
    val posX = ((screenX / 2) * 100) / 300 + model.x
    val posY = ((screenY - 200) * 100) / 300 - model.y
    (posX, posY)
  }
}
