package example.game

import indigo._
import example.PurpleBridge
import example.PurpleEvent

import scala.collection.mutable

final case class TyrianSubSystem[A](bridge: PurpleBridge[A]) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = Unit

  private val eventQueue: mutable.Queue[TyrianEvent.Receive] =
    new mutable.Queue[TyrianEvent.Receive]()

  bridge.addEventListener[PurpleEvent[A]](
    PurpleEvent.SendToIndigo,
    {
      case PurpleEvent(_, value) =>
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
      bridge.sendToTyrian(value)
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
