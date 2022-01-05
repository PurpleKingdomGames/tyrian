package example

import org.scalajs.dom.EventTarget
import org.scalajs.dom.Event
import tyrian.Sub
import util.Functions
import tyrian.Cmd

final class PurpleBridge[A]() extends EventTarget:

  def sendToIndigo(value: A): Unit =
    this.dispatchEvent(new PurpleEvent(PurpleEvent.SendToIndigo, value))

  def sendToTyrian(value: A): Unit =
    this.dispatchEvent(new PurpleEvent(PurpleEvent.SendToTyrian, value))

object PurpleBridge:

  def create[A]: PurpleBridge[A] =
    new PurpleBridge()

  // Essentially a copy of Sub.fromEvent where the values have been fixed.
  def sub[A, B](bridge: PurpleBridge[_])(extract: A => Option[B]): Sub[B] =
    Sub.ofTotalObservable[B](
      PurpleEvent.SendToTyrian + bridge.hashCode,
      { observer =>
        val listener = Functions.fun { (a: A) =>
          extract(a) match {
            case Some(b) => observer.onNext(b)
            case None    => ()
          }
        }
        bridge.addEventListener(PurpleEvent.SendToTyrian, listener)
        () => bridge.removeEventListener(PurpleEvent.SendToTyrian, listener)
      }
    )

  def send[A](bridge: PurpleBridge[A], value: A): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      bridge.sendToIndigo(value)
      ()
    }

final class PurpleEvent[A](val name: String, val value: A) extends Event(name)
object PurpleEvent:
  val SendToIndigo: String = "SendToIndigo"
  val SendToTyrian: String = "SendToTyrian"

  def unapply[A](e: PurpleEvent[A]): Option[(String, A)] =
    Some((e.name, e.value))
