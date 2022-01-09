package tyrian

import indigo.shared.Outcome
import indigo.shared.events.FrameTick
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemFrameContext

import scala.collection.mutable

final case class TyrianSubSystem[A](bridge: TyrianIndigoBridger[A]) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = Unit

  private val eventQueue: mutable.Queue[TyrianEvent.Receive] =
    new mutable.Queue[TyrianEvent.Receive]()

  bridge.addEventListener[BridgeToIndigo[A]](
    BridgeToIndigo.EventName,
    {
      case BridgeToIndigo(value) =>
        eventQueue.enqueue(TyrianEvent.Receive(value))

      case _ =>
        ()
    }
  )

  def eventFilter: GlobalEvent => Option[EventType] =
    case FrameTick      => Some(TyrianSubSystemEnqueue)
    case e: TyrianEvent => Some(e)
    case _              => None

  def initialModel: Outcome[Unit] =
    Outcome(())

  def update(context: SubSystemFrameContext, model: Unit): GlobalEvent => Outcome[Unit] =
    case TyrianEvent.Send(value) =>
      bridge.dispatchEvent(BridgeToTyrian(value))
      Outcome(model)

    case TyrianSubSystemEnqueue =>
      Outcome(model).addGlobalEvents(eventQueue.dequeueAll(_ => true).toList)

    case _ =>
      Outcome(model)

  def present(context: SubSystemFrameContext, model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

  enum TyrianEvent extends GlobalEvent:
    case Send(value: A)    extends TyrianEvent
    case Receive(value: A) extends TyrianEvent

case object TyrianSubSystemEnqueue extends GlobalEvent
