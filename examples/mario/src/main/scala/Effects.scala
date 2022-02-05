package mario

import mario.Main.*
import org.scalajs.dom
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.document
import org.scalajs.dom.raw.Event
import org.scalajs.dom.raw.HTMLAudioElement
import org.scalajs.dom.raw.TouchEvent
import org.scalajs.dom.window
import tyrian.Cmd
import tyrian.Sub
import tyrian.Sub.ofTotalObservable
import tyrian.Task

object Effects {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
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
    Sub.fromEvent[KeyboardEvent, KeyboardEvent]("keydown", dom.window) {
      event =>
        if event.keyCode == keyCode then Some(event) else None
    }

  def keyReleaseSub(keyCode: Int): Sub[KeyboardEvent] =
    Sub.fromEvent[KeyboardEvent, KeyboardEvent]("keyup", dom.window) { event =>
      if event.keyCode == keyCode then Some(event) else None
    }

  val UNDER_FRONT_MARIO  = (true, true)
  val UNDER_BEHIND_MARIO = (false, true)

  def touchPressedSub(model: Mario): Sub[Msg] =
    Sub.fromEvent[TouchEvent, Msg]("touchstart", dom.window) { event =>
      val (posX, posY) =
        modelPositionScreen(window.innerWidth, window.innerHeight, model)

      val first = event.touches.item(0)
      (posX < first.clientX / 3, posY < first.clientY / 3) match
        case UNDER_FRONT_MARIO  => Some(Msg.ArrowRightPressed)
        case UNDER_BEHIND_MARIO => Some(Msg.ArrowLeftPressed)
        case _                  => Some(Msg.ArrowUpPressed)
    }

  def touchReleasedSub(model: Mario): Sub[Msg] =
    Sub.fromEvent[TouchEvent, Msg]("touchend", dom.window) { event =>
      val (posX, posY) =
        modelPositionScreen(window.innerWidth, window.innerHeight, model)

      val first = event.changedTouches.item(0)
      (posX < first.clientX / 3, posY < first.clientY / 3) match
        case UNDER_FRONT_MARIO  => Some(Msg.ArrowRightReleased)
        case UNDER_BEHIND_MARIO => Some(Msg.ArrowLeftReleased)
        case _                  => None
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
