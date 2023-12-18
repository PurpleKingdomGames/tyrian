package tyrian

import cats.Applicative
import cats.Functor
import cats.effect.kernel.Sync
import cats.effect.kernel.Temporal
import cats.kernel.Eq
import cats.kernel.Monoid
import cats.syntax.eq.*
import tyrian.runtime.CmdHelper

import scala.annotation.targetName
import scala.concurrent.duration.FiniteDuration

/** A command describes some side-effect to perform.
  */
sealed trait Cmd[+F[_], +Msg]:
  /** Transforms the type of messages produced by the command */
  def map[OtherMsg](f: Msg => OtherMsg): Cmd[F, OtherMsg]

  /** Infix operation for combining two Cmds into one. */
  def combine[F2[x] >: F[x], LubMsg >: Msg](other: Cmd[F2, LubMsg]): Cmd[F2, LubMsg] =
    Cmd.merge(this, other)

  /** Infix operator for combining two Cmds into one. */
  def |+|[F2[x] >: F[x], LubMsg >: Msg](other: Cmd[F2, LubMsg]): Cmd[F2, LubMsg] =
    Cmd.merge(this, other)

object Cmd:
  given CanEqual[Cmd[_, _], Cmd[_, _]] = CanEqual.derived

  final def merge[F[_], Msg, LubMsg >: Msg](a: Cmd[F, Msg], b: Cmd[F, LubMsg]): Cmd[F, LubMsg] =
    (a, b) match {
      case (Cmd.None, Cmd.None) => Cmd.None
      case (Cmd.None, c2)       => c2
      case (c1, Cmd.None)       => c1
      case (c1, c2)             => Cmd.Combine[F, LubMsg](c1, c2)
    }

  def emit[Msg](msg: Msg): Cmd[Nothing, Msg] =
    Cmd.Emit(msg)

  def emitAfterDelay[F[_]: Temporal, Msg](msg: Msg, delay: FiniteDuration): Cmd[F, Msg] =
    Cmd.Run(Temporal[F].delayBy(Temporal[F].pure(msg), delay), identity)

  /** The empty command represents the absence of any command to perform */
  case object None extends Cmd[Nothing, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): None.type = this

  /** Runs a task that produces no message */
  final case class SideEffect[F[_]: Sync, A](task: F[A]) extends Cmd[F, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect[F, A] = this
    def toTask: F[Unit]                                         = Sync[F].*>(task)(Sync[F].unit)
  object SideEffect:
    def apply[F[_]: Sync, A](thunk: => A): SideEffect[F, A] =
      SideEffect(Sync[F].delay(thunk))

  /** Simply produces a message that will then be actioned. */
  final case class Emit[Msg](msg: Msg) extends Cmd[Nothing, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Emit[OtherMsg] = Emit(f(msg))
    def toTask[F[_]: Applicative]: F[Msg]                 = Applicative[F].pure(msg)

  /** Represents runnable concurrent task that produces a message */
  final case class Run[F[_]: Applicative, A, Msg](
      task: F[A],
      toMsg: A => Msg
  ) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Run[F, A, OtherMsg] = Run(task, toMsg andThen f)
    def toTask: F[Msg]                                         = Applicative[F].map(task)(toMsg)
  object Run:
    @targetName("Cmd.Run separate param lists")
    def apply[F[_]: Applicative, A, Msg](task: F[A])(toMessage: A => Msg): Run[F, A, Msg] =
      Run(task, toMessage)

    def apply[F[_]: Applicative, Msg](task: F[Msg]): Run[F, Msg, Msg] =
      Run(task, identity)

  /** Merge two commands into a single one */
  case class Combine[F[_], Msg](cmd1: Cmd[F, Msg], cmd2: Cmd[F, Msg]) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Combine[F, OtherMsg] = Combine(cmd1.map(f), cmd2.map(f))
    def toBatch: Cmd.Batch[F, Msg]                              = Cmd.Batch(List(cmd1, cmd2))

  /** Treat many commands as one */
  case class Batch[F[_], Msg](cmds: List[Cmd[F, Msg]]) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))
    def ++(other: Batch[F, Msg]): Batch[F, Msg]               = Batch(cmds ++ other.cmds)
    def ::(cmd: Cmd[F, Msg]): Batch[F, Msg]                   = Batch(cmd :: cmds)
    def +:(cmd: Cmd[F, Msg]): Batch[F, Msg]                   = Batch(cmd +: cmds)
    def :+(cmd: Cmd[F, Msg]): Batch[F, Msg]                   = Batch(cmds :+ cmd)

  object Batch:
    def apply[F[_], Msg](cmds: Cmd[F, Msg]*): Batch[F, Msg] =
      Batch(cmds.toList)

  def combineAll[F[_], A](list: List[Cmd[F, A]]): Cmd[F, A] =
    Monoid[Cmd[F, A]].combineAll(list)

  // Cats' typeclass instances

  given [F[_], Msg]: Monoid[Cmd[F, Msg]] with
    def empty: Cmd[F, Msg]                                   = Cmd.None
    def combine(a: Cmd[F, Msg], b: Cmd[F, Msg]): Cmd[F, Msg] = Cmd.merge(a, b)

  given [F[_]: Applicative, Msg: Eq](using ev: Eq[F[Option[Msg]]]): Eq[Cmd[F, Msg]] with
    def eqv(x: Cmd[F, Msg], y: Cmd[F, Msg]): Boolean =
      CmdHelper.cmdToTaskList(x) === CmdHelper.cmdToTaskList(y)

  given [F[_]]: Functor[Cmd[F, *]] with
    def map[A, B](fa: Cmd[F, A])(f: A => B): Cmd[F, B] = fa.map(f)
