package tyrian.runtime

import cats.effect.kernel.Temporal
import cats.effect.syntax.all.*
import tyrian.Cmd

import scala.annotation.tailrec

object CmdHelper:

  def cmdToTaskList[F[_]: Temporal, Msg](cmd: Cmd[F, Msg]): List[F[Option[Msg]]] =
    @tailrec
    def rec(remaining: List[Cmd[F, Msg]], acc: List[F[Option[Msg]]]): List[F[Option[Msg]]] =
      remaining match
        case Nil =>
          acc.reverse

        case cmd :: cmds =>
          cmd match
            case Cmd.None =>
              rec(cmds, acc)

            case Cmd.Emit(msg, delay) =>
              rec(cmds, Temporal[F].delayBy(Temporal[F].pure(Option(msg)), delay) :: acc)

            case Cmd.SideEffect(task) =>
              rec(cmds, Temporal[F].map(task)(_ => Option.empty[Msg]) :: acc)

            case Cmd.Run(task, f) =>
              rec(cmds, Temporal[F].map(task)(p => Option(f(p))) :: acc)

            case Cmd.Combine(cmd1, cmd2) =>
              rec(cmd1 :: cmd2 :: cmds, acc)

            case Cmd.Batch(cs) =>
              rec(cs ++ cmds, acc)

    rec(List(cmd), Nil)
