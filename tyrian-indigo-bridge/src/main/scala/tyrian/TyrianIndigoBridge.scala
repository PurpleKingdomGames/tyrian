package tyrian

import org.scalajs.dom.Event
import org.scalajs.dom.EventTarget
import tyrian.Cmd
import tyrian.Sub
import util.Functions

trait TyrianIndigoBridge[A]:
  lazy val bridge: TyrianIndigoBridger[A] =
    TyrianIndigoBridge.build

final class TyrianIndigoBridger[A]:

  val eventTarget: EventTarget = new EventTarget()

  def send(value: A): Cmd[Nothing] =
    TyrianIndigoBridge.send(this, None, value)
  def sendTo(indigoGame: IndigoGameId, value: A): Cmd[Nothing] =
    TyrianIndigoBridge.send(this, Option(indigoGame), value)

  def subscribe[B](extract: A => Option[B])(using CanEqual[B, B]): Sub[B] =
    TyrianIndigoBridge.subscribe(this, None, extract)
  def subscribeTo[B](indigoGame: IndigoGameId)(extract: A => Option[B])(using CanEqual[B, B]): Sub[B] =
    TyrianIndigoBridge.subscribe(this, Option(indigoGame), extract)

  def subSystem: TyrianSubSystem[A] =
    TyrianSubSystem(this)
  def subSystemFor(indigoGame: IndigoGameId): TyrianSubSystem[A] =
    TyrianSubSystem(Option(indigoGame), this)

object TyrianIndigoBridge:

  def build[A]: TyrianIndigoBridger[A] =
    new TyrianIndigoBridger[A]()

  // Essentially a copy of Sub.fromEvent where the values have been fixed.
  def subscribe[A, B](bridge: TyrianIndigoBridger[_], indigoGameId: Option[IndigoGameId], extract: A => Option[B])(using
      CanEqual[B, B]
  ): Sub[B] =
    val eventExtract: BridgeToTyrian[A] => Option[B] =
      e =>
        indigoGameId match
          case None                       => extract(e.value)
          case id if e.indigoGameId == id => extract(e.value)
          case _                          => None

    tyrian.Sub.ofTotalObservable[B](
      BridgeToTyrian.EventName + bridge.hashCode,
      { observer =>
        val listener = Functions.fun { (a: BridgeToTyrian[A]) =>
          eventExtract(a) match {
            case Some(b) => observer.onNext(b)
            case None    => ()
          }
        }
        bridge.eventTarget.addEventListener(BridgeToTyrian.EventName, listener)
        () => bridge.eventTarget.removeEventListener(BridgeToTyrian.EventName, listener)
      }
    )

  def send[A](bridge: TyrianIndigoBridger[A], indigoGameId: Option[IndigoGameId], value: A): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      bridge.eventTarget.dispatchEvent(BridgeToIndigo(indigoGameId, value))
      ()
    }

final class BridgeToIndigo[A](val indigoGameId: Option[IndigoGameId], val value: A)
    extends Event(BridgeToIndigo.EventName)
object BridgeToIndigo:
  val EventName: String = "SendToIndigo"

  def unapply[A](e: BridgeToIndigo[A]): Option[(Option[IndigoGameId], A)] =
    Some((e.indigoGameId, e.value))

final class BridgeToTyrian[A](val indigoGameId: Option[IndigoGameId], val value: A)
    extends Event(BridgeToTyrian.EventName)
object BridgeToTyrian:
  val EventName: String = "SendToTyrian"

  def unapply[A](e: BridgeToTyrian[A]): Option[(Option[IndigoGameId], A)] =
    Some((e.indigoGameId, e.value))

opaque type IndigoGameId = String
object IndigoGameId:
  inline def apply(id: String): IndigoGameId    = id
  def unapply(id: IndigoGameId): Option[String] = Some(id)

  given CanEqual[IndigoGameId, IndigoGameId]                 = CanEqual.derived
  given CanEqual[Option[IndigoGameId], Option[IndigoGameId]] = CanEqual.derived
