package tyrian.runtime

import cats.effect.IO
import tyrian.Cmd

import scala.annotation.tailrec

object CmdRunner:

  def cmdToTaskList[Msg](cmd: Cmd[Msg]): List[IO[Option[Msg]]] =
    @tailrec
    def rec(remaining: List[Cmd[Msg]], acc: List[IO[Option[Msg]]]): List[IO[Option[Msg]]] =
      remaining match
        case Nil =>
          acc.reverse

        case cmd :: cmds =>
          cmd match
            case Cmd.Empty =>
              rec(cmds, acc)

            case Cmd.Emit(msg) =>
              rec(cmds, IO.delay(Option(msg)) :: acc)

            case Cmd.SideEffect(task) =>
              rec(cmds, task.map(_ => Option.empty[Msg]) :: acc)

            case Cmd.Run(task, f) =>
              rec(cmds, task.map(p => Option(f(p))) :: acc)

            case Cmd.Combine(cmd1, cmd2) =>
              rec(cmd1 :: cmd2 :: cmds, acc)

            case Cmd.Batch(cs) =>
              rec(cs ++ cmds, acc)

    rec(List(cmd), Nil)
