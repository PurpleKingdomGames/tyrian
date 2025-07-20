package tyrian.next

import cats.effect.IO
import tyrian.Cmd
import tyrian.Sub

import scala.annotation.nowarn

object ActionWatcherUtils:

  extension (a: Action) def run: IO[GlobalMsg] = runCmd(a.toCmd)

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def runCmd[Msg](cmd: Cmd[IO, Msg]): IO[Msg] =
    cmd match
      case c: Cmd.Run[IO, ?, ?] =>
        c.task.map(c.toMsg)

      case Cmd.Emit(msg) =>
        IO(msg)

      case _ =>
        throw new Exception("failed, was not a run task")

  extension [A](w: Watcher) def run: (Either[Throwable, A] => Unit) => IO[Unit] = runSub(w.toSub)

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
