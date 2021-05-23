package mario

import mario.Main._
import org.scalajs.dom
import org.scalajs.dom.{KeyboardEvent, document, window}
import org.scalajs.dom.raw.{Event, HTMLAudioElement, TouchEvent}
import scalm.{Cmd, Sub, Task}
import scalm.Sub.ofTotalObservable

object Effects {

  val requestAnimationFrameSub: Sub[Double] = ofTotalObservable[Double](
    "requestAnimation",
    { observer =>
      var handle = 0
      def loop: Double => Unit = time => {
        observer.onNext(time)
        handle = dom.window.requestAnimationFrame(loop)
      }
      handle = dom.window.requestAnimationFrame(loop)
      () => dom.window.cancelAnimationFrame(handle)
    }
  )

  def keyPressSub(keyCode: Int): Sub[KeyboardEvent] =
    Sub.fromEvent[KeyboardEvent, KeyboardEvent]("keydown", dom.window) { event =>
      if (event.keyCode == keyCode) Some(event) else None
    }

  def keyReleaseSub(keyCode: Int): Sub[KeyboardEvent] =
    Sub.fromEvent[KeyboardEvent, KeyboardEvent]("keyup", dom.window) { event =>
      if (event.keyCode == keyCode) Some(event) else None
    }

  val UNDER_FRONT_MARIO  = (true, true)
  val UNDER_BEHIND_MARIO = (false, true)

  def touchPressedSub(model: Model): Sub[Msg] =
    Sub.fromEvent[TouchEvent, Msg]("touchstart", dom.window) { event =>
      val (posX, posY) =
        modelPositionScreen(window.innerWidth, window.innerHeight, model)

      val first = event.touches.item(0)
      (posX < first.clientX / 3, posY < first.clientY / 3) match {
        case UNDER_FRONT_MARIO  => Some(ArrowRightPressed)
        case UNDER_BEHIND_MARIO => Some(ArrowLeftPressed)
        case _                  => Some(ArrowUpPressed)
      }
    }

  def touchReleasedSub(model: Model): Sub[Msg] =
    Sub.fromEvent[TouchEvent, Msg]("touchend", dom.window) { event =>
      val (posX, posY) =
        modelPositionScreen(window.innerWidth, window.innerHeight, model)

      val first = event.changedTouches.item(0)
      (posX < first.clientX / 3, posY < first.clientY / 3) match {
        case UNDER_FRONT_MARIO  => Some(ArrowRightReleased)
        case UNDER_BEHIND_MARIO => Some(ArrowLeftReleased)
        case _                  => None
      }
    }

  object Cmd {
    def playSound(url: String): Cmd[Nothing] =
      Task
        .RunObservable[Nothing, Nothing] { _ =>
          val audio =
            document.createElement("audio").asInstanceOf[HTMLAudioElement]
          audio.src = url
          audio.onloadeddata = (_: Event) => audio.play()
          () => ()
        }
        .attempt(_.merge)
  }

}
