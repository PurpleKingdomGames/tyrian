package tyrian

import cats.MonoidK

/** A command describes some side-effect to perform.
  *
  * The difference with a `Task` is that a command never produces error values.
  */
sealed trait Cmd[+Msg]:
  def map[OtherMsg](f: Msg => OtherMsg): Cmd[OtherMsg]

  final def combine[LubMsg >: Msg](other: Cmd[LubMsg]): Cmd[LubMsg] =
    (this, other) match {
      case (Cmd.Empty, Cmd.Empty) => Cmd.Empty
      case (Cmd.Empty, c2)        => c2
      case (c1, Cmd.Empty)        => c1
      case (c1, c2)               => Cmd.Combine(c1, c2)
    }

object Cmd:
  given CanEqual[Cmd[_], Cmd[_]] = CanEqual.derived

  def run[Err, Success, Msg](task: Task[Err, Success], f: Either[Err, Success] => Msg): RunTask[Err, Success, Msg] =
    RunTask[Err, Success, Msg](task, f)

  def batch[Msg](cmds: Cmd[Msg]*): Batch[Msg] =
    Batch[Msg](cmds.toList)

  case object Empty extends Cmd[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Empty.type = this

  final case class SideEffect(task: Task[Nothing, Unit]) extends Cmd[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect = this

  final case class RunTask[Err, Success, Msg](task: Task[Err, Success], f: Either[Err, Success] => Msg)
      extends Cmd[Msg]:
    def map[OtherMsg](g: Msg => OtherMsg): RunTask[Err, Success, OtherMsg] = RunTask(task, f andThen g)

  case class Combine[Msg](cmd1: Cmd[Msg], cmd2: Cmd[Msg]) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Combine[OtherMsg] = Combine(cmd1.map(f), cmd2.map(f))

  case class Batch[Msg](cmds: List[Cmd[Msg]]) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))

  given MonoidK[Cmd] =
    new MonoidK[Cmd] {
      def empty[A]: Cmd[A]                                = Cmd.Empty
      def combineK[A](cmd1: Cmd[A], cmd2: Cmd[A]): Cmd[A] = cmd1.combine(cmd2)
    }
