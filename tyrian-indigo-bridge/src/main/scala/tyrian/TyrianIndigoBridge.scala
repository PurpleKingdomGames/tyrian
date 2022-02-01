package tyrian

import org.scalajs.dom.Event
import org.scalajs.dom.EventTarget
import tyrian.Cmd
import tyrian.Sub
import util.Functions

final class TyrianIndigoBridge[A]:

  val eventTarget: EventTarget = new EventTarget()

  def send(value: A): Cmd[Nothing] =
    sendToBridge(None, value)
  def sendTo(indigoGame: IndigoGameId, value: A): Cmd[Nothing] =
    sendToBridge(Option(indigoGame), value)

  def subscribe[B](extract: A => Option[B])(using CanEqual[B, B]): Sub[B] =
    subscribeToBridge(None, extract)
  def subscribeTo[B](indigoGame: IndigoGameId)(extract: A => Option[B])(using CanEqual[B, B]): Sub[B] =
    subscribeToBridge(Option(indigoGame), extract)

  def subSystem: TyrianSubSystem[A] =
    TyrianSubSystem(this)
  def subSystemFor(indigoGame: IndigoGameId): TyrianSubSystem[A] =
    TyrianSubSystem(Option(indigoGame), this)

  private def sendToBridge(indigoGameId: Option[IndigoGameId], value: A): Cmd[Nothing] =
    Cmd.SideEffect { () =>
      eventTarget.dispatchEvent(TyrianIndigoBridge.BridgeToIndigo(indigoGameId, value))
      ()
    }

  private def subscribeToBridge[B](indigoGameId: Option[IndigoGameId], extract: A => Option[B])(using
      CanEqual[B, B]
  ): Sub[B] =
    val eventExtract: TyrianIndigoBridge.BridgeToTyrian[A] => Option[B] =
      e =>
        indigoGameId match
          case None                       => extract(e.value)
          case id if e.indigoGameId == id => extract(e.value)
          case _                          => None

    tyrian.Sub.ofTotalObservable[B](
      TyrianIndigoBridge.BridgeToTyrian.EventName + this.hashCode,
      { observer =>
        val listener = Functions.fun { (a: TyrianIndigoBridge.BridgeToTyrian[A]) =>
          eventExtract(a) match {
            case Some(b) => observer.onNext(b)
            case None    => ()
          }
        }
        eventTarget.addEventListener(TyrianIndigoBridge.BridgeToTyrian.EventName, listener)
        () => eventTarget.removeEventListener(TyrianIndigoBridge.BridgeToTyrian.EventName, listener)
      }
    )

object TyrianIndigoBridge:

  def apply[A](): TyrianIndigoBridge[A] =
    new TyrianIndigoBridge[A]()

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
