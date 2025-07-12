// package tyrian

// import cats.Applicative
// import cats.Functor
// import cats.effect.IO
// import cats.kernel.Eq
// import cats.kernel.Monoid
// import cats.syntax.eq.*
// import tyrian.runtime.CmdHelper

// import scala.annotation.targetName
// import scala.concurrent.duration.FiniteDuration

// /** A command describes some side-effect to perform.
//   */
// sealed trait CmdIO[+Msg]:
//   /** Transforms the type of messages produced by the command */
//   def map[OtherMsg](f: Msg => OtherMsg): CmdIO[OtherMsg]

//   def toCmd: Cmd[IO, Msg]

//   /** Infix operation for combining two Cmds into one. */
//   def combine[LubMsg >: Msg](other: CmdIO[LubMsg]): CmdIO[LubMsg] =
//     CmdIO.fromCmd(Cmd.merge(this.toCmd, other.toCmd))

//   /** Infix operator for combining two Cmds into one. */
//   def |+|[LubMsg >: Msg](other: CmdIO[LubMsg]): CmdIO[LubMsg] =
//     combine(other)

// object CmdIO:
//   given CanEqual[CmdIO[?], CmdIO[?]] = CanEqual.derived

//   final def fromCmd[Msg](cmd: Cmd[IO, Msg]): CmdIO[Msg] =
//     cmd match
//       case Cmd.None             => CmdIO.None
//       case Cmd.Emit(msg)        => CmdIO.Emit(msg)
//       case Cmd.Run(task, toMsg) => CmdIO.Run(task, toMsg)
//       case Cmd.SideEffect(task) => CmdIO.SideEffect(task)
//       case Cmd.Combine(a, b)    => CmdIO.Combine(fromCmd(a), fromCmd(b))
//       case Cmd.Batch(cmds)      => CmdIO.Batch(cmds.map(fromCmd))

//   def emit[Msg](msg: Msg): CmdIO[Msg] =
//     fromCmd(Cmd.Emit(msg))

//   def emitAfterDelay[Msg](msg: Msg, delay: FiniteDuration): CmdIO[Msg] =
//     CmdIO.Run(IO.pure(msg).delayBy(delay), identity)

//   /** The empty command represents the absence of any command to perform */
//   case object None extends CmdIO[Nothing]:
//     def map[OtherMsg](f: Nothing => OtherMsg): None.type =
//       this

//     def toCmd: Cmd[IO, Nothing] =
//       Cmd.None

//   /** Runs a task that produces no message */
//   final case class SideEffect[A](task: IO[A]) extends CmdIO[Nothing]:
//     def map[OtherMsg](f: Nothing => OtherMsg): SideEffect[A] =
//       this

//     def toTask: IO[Unit] =
//       task.void

//     def toCmd: Cmd[IO, Nothing] =
//       Cmd.SideEffect[IO, A](task)
//   object SideEffect:
//     def apply[A](thunk: => A): SideEffect[A] =
//       SideEffect(IO.delay(thunk))

//   /** Simply produces a message that will then be actioned. */
//   final case class Emit[Msg](msg: Msg) extends CmdIO[Msg]:
//     def map[OtherMsg](f: Msg => OtherMsg): Emit[OtherMsg] =
//       Emit(f(msg))

//     def toTask[F[_]: Applicative]: F[Msg] =
//       Applicative[F].pure(msg)

//     def toCmd: Cmd[IO, Msg] =
//       Cmd.Emit[Msg](msg)

//   /** Represents runnable concurrent task that produces a message */
//   final case class Run[A, Msg](
//       task: IO[A],
//       toMsg: A => Msg
//   ) extends CmdIO[Msg]:
//     def map[OtherMsg](f: Msg => OtherMsg): Run[A, OtherMsg] =
//       Run(task, toMsg andThen f)

//     def toTask: IO[Msg] =
//       task.map(toMsg)

//     def toCmd: Cmd[IO, Msg] =
//       Cmd.Run[IO, A, Msg](task, toMsg)

//   object Run:
//     @targetName("Cmd.Run separate param lists")
//     def apply[A, Msg](task: IO[A])(toMessage: A => Msg): Run[A, Msg] =
//       Run(task, toMessage)

//     def apply[Msg](task: IO[Msg]): Run[Msg, Msg] =
//       Run(task, identity)

//   /** Merge two commands into a single one */
//   case class Combine[Msg](cmd1: CmdIO[Msg], cmd2: CmdIO[Msg]) extends CmdIO[Msg]:
//     def map[OtherMsg](f: Msg => OtherMsg): Combine[OtherMsg] =
//       Combine(cmd1.map(f), cmd2.map(f))

//     def toBatch: CmdIO.Batch[Msg] =
//       CmdIO.Batch(List(cmd1, cmd2))

//     def toCmd: Cmd[IO, Msg] =
//       Cmd.Combine(cmd1.toCmd, cmd2.toCmd)

//   /** Treat many commands as one */
//   case class Batch[Msg](cmds: List[CmdIO[Msg]]) extends CmdIO[Msg]:
//     def map[OtherMsg](f: Msg => OtherMsg): Batch[OtherMsg] = this.copy(cmds = cmds.map(_.map(f)))
//     def ++(other: Batch[Msg]): Batch[Msg]                  = Batch(cmds ++ other.cmds)
//     def ::(cmd: CmdIO[Msg]): Batch[Msg]                    = Batch(cmd :: cmds)
//     def +:(cmd: CmdIO[Msg]): Batch[Msg]                    = Batch(cmd +: cmds)
//     def :+(cmd: CmdIO[Msg]): Batch[Msg]                    = Batch(cmds :+ cmd)

//     def toCmd: Cmd[IO, Msg] =
//       Cmd.Batch[IO, Msg](cmds.map(_.toCmd))

//   object Batch:
//     def apply[Msg](cmds: CmdIO[Msg]*): Batch[Msg] =
//       Batch(cmds.toList)

//   def combineAll[A](list: List[CmdIO[A]]): CmdIO[A] =
//     Monoid[CmdIO[A]].combineAll(list)

//   // Cats' typeclass instances

//   given [Msg]: Monoid[CmdIO[Msg]] with
//     def empty: CmdIO[Msg]                                 = CmdIO.None
//     def combine(a: CmdIO[Msg], b: CmdIO[Msg]): CmdIO[Msg] = a.combine(b)

//   given [Msg](using ev: Eq[IO[Option[Msg]]]): Eq[CmdIO[Msg]] with
//     def eqv(x: CmdIO[Msg], y: CmdIO[Msg]): Boolean =
//       CmdHelper.cmdToTaskList(x.toCmd) === CmdHelper.cmdToTaskList(y.toCmd)

//   given Functor[CmdIO[*]] with
//     def map[A, B](fa: CmdIO[A])(f: A => B): CmdIO[B] = fa.map(f)
