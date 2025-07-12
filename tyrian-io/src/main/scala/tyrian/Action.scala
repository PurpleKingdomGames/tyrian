package tyrian

import cats.Applicative
import cats.effect.IO
import cats.kernel.Eq
import cats.kernel.Monoid

import scala.annotation.targetName
import scala.concurrent.duration.FiniteDuration

/** A command describes some side-effect to perform.
  */
sealed trait Action:
  /** Transforms the type of messages produced by the command */
  def map(f: GlobalMsg => GlobalMsg): Action

  def toCmd: Cmd[IO, GlobalMsg]

  /** Infix operation for combining two Cmds into one. */
  def combine(other: Action): Action =
    Action.fromCmd(Cmd.merge(this.toCmd, other.toCmd))

  /** Infix operator for combining two Cmds into one. */
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
      case Cmd.Combine(a, b)    => Action.Combine(fromCmd(a), fromCmd(b))
      case Cmd.Batch(cmds)      => Action.Batch(cmds.map(fromCmd))

  def emit(msg: GlobalMsg): Action =
    fromCmd(Cmd.Emit(msg))

  def emitAfterDelay(msg: GlobalMsg, delay: FiniteDuration): Action =
    Action.Run(IO.pure(msg).delayBy(delay), identity)

  /** The empty command represents the absence of any command to perform */
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

    def toTask[F[_]: Applicative]: F[GlobalMsg] =
      Applicative[F].pure(msg)

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

  /** Merge two commands into a single one */
  case class Combine(cmd1: Action, cmd2: Action) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Combine =
      Combine(cmd1.map(f), cmd2.map(f))

    def toBatch: Action.Batch =
      Action.Batch(List(cmd1, cmd2))

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Combine(cmd1.toCmd, cmd2.toCmd)

  /** Treat many commands as one */
  case class Batch(cmds: List[Action]) extends Action:
    def map(f: GlobalMsg => GlobalMsg): Batch = this.copy(cmds = cmds.map(_.map(f)))
    def ++(other: Batch): Batch               = Batch(cmds ++ other.cmds)
    def ::(cmd: Action): Batch                 = Batch(cmd :: cmds)
    def +:(cmd: Action): Batch                 = Batch(cmd +: cmds)
    def :+(cmd: Action): Batch                 = Batch(cmds :+ cmd)

    def toCmd: Cmd[IO, GlobalMsg] =
      Cmd.Batch(cmds.map(_.toCmd))

  object Batch:
    def apply(cmds: Action*): Batch =
      Batch(cmds.toList)

  def combineAll[A](list: List[Action]): Action =
    Monoid[Action].combineAll(list)

  // Cats' typeclass instances

  given Monoid[Action] with
    def empty: Action                       = Action.None
    def combine(a: Action, b: Action): Action = a.combine(b)

  given (using eq: Eq[Cmd[IO, GlobalMsg]]): Eq[Action] with
    def eqv(x: Action, y: Action): Boolean =
      eq(x, y)
