package tyrian.next

import cats.effect.IO
import tyrian.Cmd

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

  /** Converts an Action into a Cmd */
  def toCmd: Cmd[IO, GlobalMsg]

object Action:
  given CanEqual[Action, Action] = CanEqual.derived

  /** The empty action that performs no side effects. */
  def none: Action =
    Action.None

  /** Converts a Cmd into an Action. */
  def fromCmd(cmd: Cmd[IO, GlobalMsg]): Action =
    cmd match
      case Cmd.None             => Action.None
      case Cmd.Emit(msg)        => Action.Emit(msg)
      case Cmd.Run(task, toMsg) => Action.Run(task, toMsg)
      case Cmd.SideEffect(task) => Action.SideEffect(task)
      case Cmd.Combine(a, b)    => Action.Many(Batch(fromCmd(a), fromCmd(b)))
      case Cmd.Batch(cmds)      => Action.Many(Batch.fromList(cmds).map(fromCmd))

  /** Creates an action that immediately emits a message. */
  def emit(msg: GlobalMsg): Action =
    fromCmd(Cmd.Emit(msg))

  /** Creates an action that emits a message after a specified delay. */
  def emitAfterDelay(msg: GlobalMsg, delay: FiniteDuration): Action =
    Action.Run(IO.pure(msg).delayBy(delay), identity)

  /** Creates an action that runs a side effect without producing a message. */
  def sideEffect(thunk: => Unit): Action =
    Action.SideEffect(thunk)

  /** Alias for sideEffect. */
  def fireAndForget(thunk: => Unit): Action =
    sideEffect(thunk)

  /** Alias for sideEffect. */
  def thunk(run: => Unit): Action =
    sideEffect(run)

  /** Creates an action that runs a process and emits the result as a message. */
  def run(process: () => GlobalMsg): Action =
    Action.Run(process)

  /** Creates an action that runs a process and transforms the result into a message. */
  def run[A](process: () => A)(toMsg: A => GlobalMsg): Action =
    Action.Run(process)(toMsg)

  // Smart constructors

  /** Run a Cmd */
  def apply(cmd: Cmd[IO, GlobalMsg]): Action =
    fromCmd(cmd)

  /** Emit a GlobalMsg */
  def apply(msg: GlobalMsg): Action =
    emit(msg)

  /** Run a side effect */
  def apply(thunk: => Unit): Action =
    Action.SideEffect(thunk)

  /** The empty action represents the absence of any action to perform */
  private[next] case object None extends Action:
    def map(f: GlobalMsg => GlobalMsg): None.type =
      this

    def toCmd: Cmd[IO, Nothing] =
      Cmd.None

  /** Runs a task that produces no message */
  private[next] final case class SideEffect[A](task: IO[A]) extends Action:
    def map(f: GlobalMsg => GlobalMsg): SideEffect[A] =
      this

    def toTask: IO[Unit] =
      task.void

    def toCmd: Cmd[IO, Nothing] =
      Cmd.SideEffect[IO, A](task)
  private[next] object SideEffect:
    def apply[A](thunk: => A): SideEffect[A] =
      SideEffect(IO.delay(thunk))

  /** Simply produces a message that will then be actioned. */
  private[next] final case class Emit(msg: GlobalMsg) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Emit =
      Emit(f(msg))

    def toTask: IO[GlobalMsg] =
      IO(msg)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Emit[GlobalMsg](msg)

  /** Represents runnable concurrent task that produces a message */
  private[next] final case class Run[A](
      task: IO[A],
      toMsg: A => GlobalMsg
  ) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Run[A] =
      Run(task, toMsg andThen f)

    def toTask: IO[GlobalMsg] =
      task.map(toMsg)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Run[IO, A, GlobalMsg](task, toMsg)

  private[next] object Run:
    def apply[A](run: () => A)(toMessage: A => GlobalMsg): Run[A] =
      Run(IO(run()), toMessage)

    def apply(run: () => GlobalMsg): Run[GlobalMsg] =
      Run(IO(run()), identity)

  /** Treat many actions as one, kept here to support Cmd conversion to Action, but usage is discouraged. */
  private[next] final case class Many(actions: Batch[Action]) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Many = this.copy(actions = actions.map(_.map(f)))
    def ++(other: Many): Many                = Many(actions ++ other.actions)
    def ::(action: Action): Many             = Many(action :: actions)
    def +:(action: Action): Many             = Many(action +: actions)
    def :+(action: Action): Many             = Many(actions :+ action)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Batch(actions.map(_.toCmd).toList)

  private[next] object Many:
    def apply(cmds: Action*): Many =
      Many(Batch.fromSeq(cmds))
