package tyrian.runtime

import cats.Applicative
import tyrian.Cmd

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

object CmdHelper:

  def cmdToTaskList[F[_]: Applicative, Msg](cmd: Cmd[F, Msg]): List[F[Option[Msg]]] =
    val acc: ListBuffer[F[Option[Msg]]] = ListBuffer.empty

    @tailrec
    def rec(remaining: List[Cmd[F, Msg]]): List[F[Option[Msg]]] =
      remaining match
        case Nil =>
          acc.toList

        case cmd :: cmds =>
          cmd match
            case Cmd.None =>
              rec(cmds)

            case c: Cmd.Emit[_] =>
              acc += Applicative[F].map(c.toTask)(Option.apply)
              rec(cmds)

            case c: Cmd.SideEffect[_, _] =>
              acc += Applicative[F].map(c.toTask)(_ => Option.empty[Msg])
              rec(cmds)

            case c: Cmd.Run[_, _, _] =>
              acc += Applicative[F].map(c.toTask)(Option.apply)
              rec(cmds)

            case Cmd.Combine(cmd1, cmd2) =>
              rec(cmd1 :: cmd2 :: cmds)

            case Cmd.Batch(cs) =>
              rec(cs ++ cmds)

    rec(List(cmd))
