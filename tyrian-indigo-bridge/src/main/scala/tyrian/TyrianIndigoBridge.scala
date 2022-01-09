package tyrian

import org.scalajs.dom.Event
import org.scalajs.dom.EventTarget
import tyrian.Cmd
import tyrian.Sub
import util.Functions

trait TyrianIndigoBridge:
  lazy val bridge: TyrianIndigoBridger[String] =
    TyrianIndigoBridge.build

final class TyrianIndigoBridger[A] extends EventTarget:

  def send(value: A): Cmd[Nothing] =
    TyrianIndigoBridge.send(this, value)

  def subscribe[B](extract: A => Option[B])(using CanEqual[B, B]): Sub[B] =
    TyrianIndigoBridge.subscribe(this, extract)

  def subSystem: TyrianSubSystem[A] =
    TyrianSubSystem(this)

object TyrianIndigoBridge:

  def build[A]: TyrianIndigoBridger[A] =
    new TyrianIndigoBridger

  // Essentially a copy of Sub.fromEvent where the values have been fixed.
  def subscribe[A, B](bridge: TyrianIndigoBridger[_], extract: A => Option[B])(using CanEqual[B, B]): Sub[B] =
    val eventExtract: BridgeToTyrian[A] => Option[B] =
      e => extract(e.value)
    tyrian.Sub.ofTotalObservable[B](
      BridgeToTyrian.EventName + bridge.hashCode,
      { observer =>
        val listener = Functions.fun { (a: BridgeToTyrian[A]) =>
          eventExtract(a) match {
            case Some(b) => observer.onNext(b)
            case None    => ()
          }
        }
        bridge.addEventListener(BridgeToTyrian.EventName, listener)
        () => bridge.removeEventListener(BridgeToTyrian.EventName, listener)
      }
    )

  def send[A](bridge: TyrianIndigoBridger[A], value: A): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      bridge.dispatchEvent(BridgeToIndigo(value))
      ()
    }

final class BridgeToIndigo[A](val value: A) extends Event(BridgeToIndigo.EventName)
object BridgeToIndigo:
  val EventName: String = "SendToIndigo"

  def unapply[A](e: BridgeToIndigo[A]): Option[A] =
    Some(e.value)

final class BridgeToTyrian[A](val value: A) extends Event(BridgeToTyrian.EventName)
object BridgeToTyrian:
  val EventName: String = "SendToTyrian"

  def unapply[A](e: BridgeToTyrian[A]): Option[A] =
    Some(e.value)
