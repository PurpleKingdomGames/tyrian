package tyrian.runtime

import cats.Applicative
import tyrian.Cmd

import scala.annotation.tailrec

object CmdHelper:

  def cmdToTaskList[F[_]: Applicative, Msg](cmd: Cmd[F, Msg]): List[F[Option[Msg]]] =
    @tailrec
    def rec(remaining: List[Cmd[F, Msg]], acc: List[F[Option[Msg]]]): List[F[Option[Msg]]] =
      remaining match
        case Nil =>
          acc.reverse

        case cmd :: cmds =>
          cmd match
            case Cmd.None =>
              rec(cmds, acc)

            case c: Cmd.Emit[_] =>
              rec(cmds, Applicative[F].map(c.toTask)(Option.apply) :: acc)

            case c: Cmd.SideEffect[_] =>
              rec(cmds, Applicative[F].map(c.toTask)(_ => Option.empty[Msg]) :: acc)

            case c: Cmd.Run[_, _, _] =>
              rec(cmds, Applicative[F].map(c.toTask)(Option.apply) :: acc)

            case Cmd.Combine(cmd1, cmd2) =>
              rec(cmd1 :: cmd2 :: cmds, acc)

            case Cmd.Batch(cs) =>
              rec(cs ++ cmds, acc)

    rec(List(cmd), Nil)
