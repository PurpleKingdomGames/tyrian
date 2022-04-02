package tyrian

import cats.effect.kernel.Async

import scala.annotation.targetName

/** A command describes some side-effect to perform.
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
// enum Cmd[F[_]: Async, +Msg]:

//   extension [MMsg](cmd: Cmd[F, MMsg])
//     def map[OtherMsg](f: MMsg => OtherMsg): Cmd[F, OtherMsg] =
//       cmd match
//         case c: Cmd.Empty[F]                    => c
//         case c: Cmd.SideEffect[F]               => c
//         case c: Cmd.Emit[F, MMsg] @unchecked    => Cmd.Emit(f(c.msg)) // c.map(f)
//         case c: Cmd.Run[F, _, MMsg] @unchecked  => Cmd.Run(c.observable, c.toMessage andThen f)
//         case c: Cmd.Combine[F, MMsg] @unchecked => Cmd.Combine(c.cmd1.map(f), c.cmd2.map(f))
//         case c: Cmd.Batch[F, MMsg] @unchecked   => c.copy(cmds = c.cmds.map(_.map(f)))

//     final def combine[LubMsg >: MMsg](other: Cmd[F, LubMsg]): Cmd[F, LubMsg] =
//       (cmd, other) match {
//         case (Cmd.Empty(), Cmd.Empty()) => Cmd.Empty()
//         case (Cmd.Empty(), c2)          => c2
//         case (c1, Cmd.Empty())          => c1
//         case (c1, c2)                   => Cmd.Combine(c1, c2)
//       }

//     final def |+|[LubMsg >: MMsg](other: Cmd[F, LubMsg]): Cmd[F, LubMsg] =
//       combine(cmd)(other)

//   given CanEqual[Cmd[_, _], Cmd[_, _]] = CanEqual.derived

//   case Empty[F[_]: Async]()                                            extends Cmd[F, Nothing]
//   case SideEffect[F[_]: Async](task: F[Unit])                          extends Cmd[F, Nothing]
//   case Emit[F[_]: Async, Msg](msg: Msg)                                extends Cmd[F, Msg]
//   case Run[F[_]: Async, A, Msg](observable: F[A], toMessage: A => Msg) extends Cmd[F, Msg]
//   case Combine[F[_]: Async, Msg](cmd1: Cmd[F, Msg], cmd2: Cmd[F, Msg]) extends Cmd[F, Msg]
//   case Batch[F[_]: Async, Msg](cmds: List[Cmd[F, Msg]])                extends Cmd[F, Msg]

// object Cmd:
  def empty[F[_]: Async]: Cmd.Empty[F]                              = Cmd.Empty()
//   def sideEffect[F[_]: Async](thunk: () => Unit): Cmd.SideEffect[F] = Cmd.SideEffect(Async[F].delay(thunk()))
//   def thunk[F[_]: Async](thunk: () => Unit): Cmd.SideEffect[F]      = sideEffect(thunk)
  def run[F[_]: Async, A, Msg](observable: F[A])(toMessage: A => Msg): Cmd.Run[F, A, Msg] =
    Cmd.Run(observable, toMessage)
//   def batch[F[_]: Async, Msg](cmds: Cmd[F, Msg]*): Cmd.Batch[F, Msg] = Cmd.Batch(cmds.toList)
