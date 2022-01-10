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
  final def |+|[LubMsg >: Msg](other: Cmd[LubMsg]): Cmd[LubMsg] =
    combine(other)

object Cmd:
  given CanEqual[Cmd[_], Cmd[_]] = CanEqual.derived

  case object Empty extends Cmd[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Empty.type = this

  final case class SideEffect(task: Task.SideEffect) extends Cmd[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect = this
  object SideEffect:
    def apply(thunk: () => Unit): SideEffect =
      SideEffect(Task.SideEffect(thunk))

  final case class Emit[Msg](msg: Msg) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Emit[OtherMsg] = Emit(f(msg))

  final case class Run[Err, Success, Msg](
      observable: Task.Observable[Err, Success],
      toMessage: Either[Err, Success] => Msg
  ) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Run[Err, Success, OtherMsg] = Run(observable, toMessage andThen f)
    def attempt[OtherMsg](resultToMessage: Either[Err, Success] => OtherMsg): Run[Err, Success, OtherMsg] =
      Run(observable, resultToMessage)
  object Run:

    def apply[Err, Success, Msg](toMessage: Either[Err, Success] => Msg)(
        observable: Task.Observable[Err, Success]
    ): Run[Err, Success, Msg] =
      Run(observable, toMessage)

    final case class ImcompleteRunCmd[Err, Success](observable: Task.Observable[Err, Success]):
      def attempt[Msg](resultToMessage: Either[Err, Success] => Msg): Run[Err, Success, Msg] =
        Run(observable, resultToMessage)

    def apply[Err, Success](observable: Task.Observable[Err, Success]): ImcompleteRunCmd[Err, Success] =
      ImcompleteRunCmd(observable)

  final case class RunTask[Err, Success, Msg](task: Task[Err, Success], toMessage: Either[Err, Success] => Msg)
      extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): RunTask[Err, Success, OtherMsg] = RunTask(task, toMessage andThen f)

  case class Combine[Msg](cmd1: Cmd[Msg], cmd2: Cmd[Msg]) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Combine[OtherMsg] = Combine(cmd1.map(f), cmd2.map(f))

  case class Batch[Msg](cmds: List[Cmd[Msg]]) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))
  object Batch:
    def apply[Msg](cmds: Cmd[Msg]*): Batch[Msg] =
      Batch(cmds.toList)

  given MonoidK[Cmd] =
    new MonoidK[Cmd] {
      def empty[A]: Cmd[A]                                = Cmd.Empty
      def combineK[A](cmd1: Cmd[A], cmd2: Cmd[A]): Cmd[A] = cmd1.combine(cmd2)
    }
