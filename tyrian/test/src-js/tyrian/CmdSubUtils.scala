package tyrian

import cats.effect.IO

import scala.annotation.nowarn

object CmdSubUtils:

  extension [A](cmd: Cmd[IO, A]) def run: IO[A] = runCmd(cmd)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def runCmd[Msg](cmd: Cmd[IO, Msg]): IO[Msg] =
    cmd match
      case c: Cmd.Run[IO, ?, ?] =>
        c.task.map(c.toMsg)

      case Cmd.Emit(msg) =>
        IO(msg)

      case _ =>
        throw new Exception("failed, was not a run task")

  extension [A](sub: Sub[IO, A]) def run: (Either[Throwable, A] => Unit) => IO[Unit] = runSub(sub)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  @nowarn("msg=unused")
  def runSub[A, Msg](sub: Sub[IO, Msg])(callback: Either[Throwable, A] => Unit): IO[Unit] =
    sub match
      case s: Sub.Observe[IO, A, Msg] @unchecked =>
        s.observable.map { run =>
          run(callback)
          ()
        }

      case _ =>
        throw new Exception("failed, was not a run task")
