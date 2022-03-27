package tyrian

import cats.effect.IO

import scala.annotation.targetName

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

  final case class SideEffect(task: IO[Unit]) extends Cmd[Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect = this
  object SideEffect:
    def apply(thunk: () => Unit): SideEffect =
      SideEffect(IO(thunk()))

  final case class Emit[Msg](msg: Msg) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Emit[OtherMsg] = Emit(f(msg))

  final case class Run[A, Msg](
      observable: IO[A],
      toMessage: A => Msg
  ) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Run[A, OtherMsg] = Run(observable, toMessage andThen f)
    def attempt[OtherMsg](resultToMessage: A => OtherMsg): Run[A, OtherMsg] =
      Run(observable, resultToMessage)
  object Run:

    @targetName("Cmd.Run separate param lists")
    def apply[A, Msg](observable: IO[A])(toMessage: A => Msg): Run[A, Msg] =
      Run(observable, toMessage)

  case class Combine[Msg](cmd1: Cmd[Msg], cmd2: Cmd[Msg]) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Combine[OtherMsg] = Combine(cmd1.map(f), cmd2.map(f))

  case class Batch[Msg](cmds: List[Cmd[Msg]]) extends Cmd[Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))
  object Batch:
    def apply[Msg](cmds: Cmd[Msg]*): Batch[Msg] =
      Batch(cmds.toList)
