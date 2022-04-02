package tyrian.runtime

import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import tyrian.Cmd

import scala.annotation.tailrec

object CmdRunner:

  given CanEqual[List[Cmd[_, _]], List[Cmd[_, _]]] = CanEqual.derived

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def runCmd[F[_]: Async, Msg](
      cmd: Cmd[F, Msg],
      callback: Msg => Unit,
      async: (=> Unit) => Unit
  ): F[Unit] =
    println("runCmd: " + cmd.toString)
    @tailrec
    def loop(cmds: List[Cmd[F, Msg]], acc: List[F[Unit]]): List[F[Unit]] =
      cmds match
        case Nil =>
          acc

        case cmd :: cs =>
          cmd match
            case Cmd.Empty() =>
              loop(cs, acc)

            case Cmd.Emit(msg) =>
              loop(cs, Async[F].delay(async(callback(msg))) :: acc)

            case Cmd.SideEffect(task) =>
              println("add side effect")
              loop(cs, task :: acc)

            case Cmd.Run(obs, f) =>
              val task =
                Async[F].map(Async[F].attempt(obs)) {
                  case Right(v) => async((f andThen callback)(v))
                  case Left(e)  => throw e
                }
              loop(cs, task :: acc)

            case Cmd.Combine(cmd1, cmd2) =>
              loop(cmd1 :: cmd2 :: cs, acc)

            case Cmd.Batch(batch) =>
              loop(batch ++ cs, acc)

    val toRun =
      loop(List(cmd), Nil)

    if toRun.isEmpty then
      println("a)")
      Async[F].delay(())
    else
      println("b) length: " + toRun.length)
      // Dispatcher[F].use { dispatcher =>
      //   Async[F].delay {
      //     toRun.foreach { task =>
      //       dispatcher.unsafeToFuture(task).foreach { _ =>
      //         println("do the thing!")
      //       }
      //     }
      //   }
      // }
      toRun.sequence.map(_ => ())
// toRun
//   .traverse(_.attempt.map {
//     case Left(e)  => println(e.getMessage)
//     case Right(_) => ()
//   })
//   .map(_.foldLeft(())((_, x) => x))
// Async[F].delay(())
