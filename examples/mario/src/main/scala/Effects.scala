package mario

import cats.effect.IO
import mario.Main.*
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.HTMLAudioElement
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.TouchEvent
import org.scalajs.dom.document
import org.scalajs.dom.window
import tyrian.*

object Effects:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  val requestAnimationFrameSub: Sub[IO, Double] =
    Sub.make[IO, Double, Double, Int]("requestAnimation") { callback =>
      var handle = 0
      def loop: Double => Unit = time => {
        callback(Right(time))
        handle = dom.window.requestAnimationFrame(loop)
      }
      handle = dom.window.requestAnimationFrame(loop)
      IO(handle)
    } { handle =>
      IO(dom.window.cancelAnimationFrame(handle))
    } { t =>
      Some(t)
    }

  def keyPressSub(keyCode: Int): Sub[IO, KeyboardEvent] =
    Sub.fromEvent[IO, KeyboardEvent, KeyboardEvent]("keydown", dom.window) {
      event =>
        if event.keyCode == keyCode then Some(event) else None
    }

  def keyReleaseSub(keyCode: Int): Sub[IO, KeyboardEvent] =
    Sub.fromEvent[IO, KeyboardEvent, KeyboardEvent]("keyup", dom.window) {
      event =>
        if event.keyCode == keyCode then Some(event) else None
    }

  val UNDER_FRONT_MARIO  = (true, true)
  val UNDER_BEHIND_MARIO = (false, true)

  def touchPressedSub(model: Mario): Sub[IO, Msg] =
    Sub.fromEvent[IO, TouchEvent, Msg]("touchstart", dom.window) { event =>
      val (posX, posY) =
        modelPositionScreen(window.innerWidth, window.innerHeight, model)

      val first = event.touches.item(0)
      (posX < first.clientX / 3, posY < first.clientY / 3) match
        case UNDER_FRONT_MARIO  => Some(Msg.ArrowRightPressed)
        case UNDER_BEHIND_MARIO => Some(Msg.ArrowLeftPressed)
        case _                  => Some(Msg.ArrowUpPressed)
    }

  def touchReleasedSub(model: Mario): Sub[IO, Msg] =
    Sub.fromEvent[IO, TouchEvent, Msg]("touchend", dom.window) { event =>
      val (posX, posY) =
        modelPositionScreen(window.innerWidth, window.innerHeight, model)

      val first = event.changedTouches.item(0)
      (posX < first.clientX / 3, posY < first.clientY / 3) match
        case UNDER_FRONT_MARIO  => Some(Msg.ArrowRightReleased)
        case UNDER_BEHIND_MARIO => Some(Msg.ArrowLeftReleased)
        case _                  => None
    }

  def playSound(url: String): Cmd[IO, Nothing] =
    Cmd.SideEffect {
      val audio =
        document.createElement("audio").asInstanceOf[HTMLAudioElement]
      audio.src = url
      audio.onloadeddata = (_: Event) => audio.play()
    }
