package tyrian.next

import cats.effect.IO
import tyrian.Cmd

import scala.annotation.targetName
import scala.concurrent.duration.FiniteDuration

/** An action describes some side-effect that can be performed as a result of a model update or other GlobalMsg being
  * processed.
  *
  * Actions are Cmd's with the `F` type fixed to a known effect type, like `IO` or `Task`, and the `Msg` type fixed to
  * `GlobalMsg`.
  */
sealed trait Action:
  /** Transforms the type of messages produced by the action */
  def map(f: GlobalMsg => GlobalMsg): Action

  def toCmd: Cmd[IO, GlobalMsg]

  /** Infix operation for combining two Actions into one. */
  def combine(other: Action): Action =
    Action.fromCmd(Cmd.merge(this.toCmd, other.toCmd))

  /** Infix operator for combining two Actions into one. */
  def |+|(other: Action): Action =
    combine(other)

object Action:
  given CanEqual[Action, Action] = CanEqual.derived

  final def fromCmd(cmd: Cmd[IO, GlobalMsg]): Action =
    cmd match
      case Cmd.None             => Action.None
      case Cmd.Emit(msg)        => Action.Emit(msg)
      case Cmd.Run(task, toMsg) => Action.Run(task, toMsg)
      case Cmd.SideEffect(task) => Action.SideEffect(task)
      case Cmd.Combine(a, b)    => Action.Many(Batch(fromCmd(a), fromCmd(b)))
      case Cmd.Batch(cmds)      => Action.Many(Batch.fromList(cmds).map(fromCmd))

  def apply(cmd: Cmd[IO, GlobalMsg]): Action =
    fromCmd(cmd)

  def emit(msg: GlobalMsg): Action =
    fromCmd(Cmd.Emit(msg))

  def emitAfterDelay(msg: GlobalMsg, delay: FiniteDuration): Action =
    Action.Run(IO.pure(msg).delayBy(delay), identity)

  /** The empty action represents the absence of any action to perform */
  case object None extends Action:
    def map(f: GlobalMsg => GlobalMsg): None.type =
      this

    def toCmd: Cmd[IO, Nothing] =
      Cmd.None

  /** Runs a task that produces no message */
  final case class SideEffect[A](task: IO[A]) extends Action:
    def map(f: GlobalMsg => GlobalMsg): SideEffect[A] =
      this

    def toTask: IO[Unit] =
      task.void

    def toCmd: Cmd[IO, Nothing] =
      Cmd.SideEffect[IO, A](task)
  object SideEffect:
    def apply[A](thunk: => A): SideEffect[A] =
      SideEffect(IO.delay(thunk))

  /** Simply produces a message that will then be actioned. */
  final case class Emit(msg: GlobalMsg) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Emit =
      Emit(f(msg))

    def toTask: IO[GlobalMsg] =
      IO(msg)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Emit[GlobalMsg](msg)

  /** Represents runnable concurrent task that produces a message */
  final case class Run[A](
      task: IO[A],
      toMsg: A => GlobalMsg
  ) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Run[A] =
      Run(task, toMsg andThen f)

    def toTask: IO[GlobalMsg] =
      task.map(toMsg)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Run[IO, A, GlobalMsg](task, toMsg)

  object Run:
    @targetName("Cmd.Run separate param lists")
    def apply[A](task: IO[A])(toMessage: A => GlobalMsg): Run[A] =
      Run(task, toMessage)

    def apply(task: IO[GlobalMsg]): Run[GlobalMsg] =
      Run(task, identity)

  /** Treat many actions as one */
  final case class Many(actions: Batch[Action]) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Many = this.copy(actions = actions.map(_.map(f)))
    def ++(other: Many): Many                = Many(actions ++ other.actions)
    def ::(action: Action): Many             = Many(action :: actions)
    def +:(action: Action): Many             = Many(action +: actions)
    def :+(action: Action): Many             = Many(actions :+ action)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Batch(actions.map(_.toCmd).toList)

  object Many:
    def apply(cmds: Action*): Many =
      Many(Batch.fromSeq(cmds))

  def combineAll(list: Batch[Action]): Action =
    Action.fromCmd(
      Cmd.combineAll[IO, GlobalMsg](list.toList.map(_.toCmd))
    )
