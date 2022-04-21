package tyrian

import cats.effect.kernel.Sync
import cats.kernel.Monoid

import scala.annotation.targetName

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

  /** The empty command represents the absence of any command to perform */
  case object None extends Cmd[Nothing, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): None.type = this

  /** Runs a task that produces no message */
  final case class SideEffect[F[_]](task: F[Unit]) extends Cmd[F, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect[F] = this
  object SideEffect:
    def apply[F[_]: Sync](thunk: => Unit): SideEffect[F] =
      SideEffect(Sync[F].delay(thunk))

  /** Simply produces a message that will then be actioned. */
  final case class Emit[Msg](msg: Msg) extends Cmd[Nothing, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Emit[OtherMsg] = Emit(f(msg))

  /** Represents runnable concurrent task that produces a message */
  final case class Run[F[_], A, Msg](
      task: F[A],
      toMsg: A => Msg
  ) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Run[F, A, OtherMsg] = Run(task, toMsg andThen f)
  object Run:
    @targetName("Cmd.Run separate param lists")
    def apply[F[_], A, Msg](task: F[A])(toMessage: A => Msg): Run[F, A, Msg] =
      Run(task, toMessage)

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

  given [F[_], Msg]: Monoid[Cmd[F, Msg]] = new Monoid[Cmd[F, Msg]] {
    def empty: Cmd[F, Msg]                                   = Cmd.None
    def combine(a: Cmd[F, Msg], b: Cmd[F, Msg]): Cmd[F, Msg] = Cmd.merge(a, b)
  }

  def combineAll[F[_], A](list: List[Cmd[F, A]]): Cmd[F, A] =
    Monoid[Cmd[F, A]].combineAll(list)
