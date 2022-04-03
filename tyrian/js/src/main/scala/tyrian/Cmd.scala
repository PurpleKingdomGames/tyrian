package tyrian

import cats.effect.kernel.Async

import scala.annotation.targetName

/** A command describes some side-effect to perform.
  *
  * The difference with a `Task` is that a command never produces error values.
  */
sealed trait Cmd[F[_]: Async, +Msg]:
  def map[OtherMsg](f: Msg => OtherMsg): Cmd[F, OtherMsg]

  final def combine[LubMsg >: Msg](other: Cmd[F, LubMsg]): Cmd[F, LubMsg] =
    (this, other) match {
      case (Cmd.Empty(), Cmd.Empty()) => Cmd.Empty()
      case (Cmd.Empty(), c2)          => c2
      case (c1, Cmd.Empty())          => c1
      case (c1, c2)                   => Cmd.Combine(c1, c2)
    }
  final def |+|[LubMsg >: Msg](other: Cmd[F, LubMsg]): Cmd[F, LubMsg] =
    combine(other)

object Cmd:
  given CanEqual[Cmd[_, _], Cmd[_, _]] = CanEqual.derived

  final case class Empty[F[_]: Async]() extends Cmd[F, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): Empty[F] = this
  def empty[F[_]: Async]: Empty[F] = Empty()

  final case class SideEffect[F[_]: Async](task: F[Unit]) extends Cmd[F, Nothing]:
    def map[OtherMsg](f: Nothing => OtherMsg): SideEffect[F] = this
  object SideEffect:
    def apply[F[_]: Async](thunk: () => Unit): SideEffect[F] =
      SideEffect(Async[F].delay(thunk()))

  final case class Emit[F[_]: Async, Msg](msg: Msg) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Emit[F, OtherMsg] = Emit(f(msg))

  final case class Run[F[_]: Async, A, Msg](
      observable: F[A],
      toMessage: A => Msg
  ) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Run[F, A, OtherMsg] = Run(observable, toMessage andThen f)
    def attempt[OtherMsg](resultToMessage: A => OtherMsg): Run[F, A, OtherMsg] =
      Run(observable, resultToMessage)
  object Run:

    @targetName("Cmd.Run separate param lists")
    def apply[F[_]: Async, A, Msg](observable: F[A])(toMessage: A => Msg): Run[F, A, Msg] =
      Run(observable, toMessage)

  case class Combine[F[_]: Async, Msg](cmd1: Cmd[F, Msg], cmd2: Cmd[F, Msg]) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Combine[F, OtherMsg] = Combine(cmd1.map(f), cmd2.map(f))

  case class Batch[F[_]: Async, Msg](cmds: List[Cmd[F, Msg]]) extends Cmd[F, Msg]:
    def map[OtherMsg](f: Msg => OtherMsg): Batch[F, OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))
  object Batch:
    def apply[F[_]: Async, Msg](cmds: Cmd[F, Msg]*): Batch[F, Msg] =
      Batch(cmds.toList)
