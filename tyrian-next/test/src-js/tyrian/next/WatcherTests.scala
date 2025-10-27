package tyrian.next

import cats.effect.IO

@SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
class WatcherTests extends munit.CatsEffectSuite {

  type Obs[A] = IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]

  final case class IntMsg(i: Int) extends GlobalMsg

  import ActionWatcherUtils.*

  test("map - Empty") {
    assertEquals(Watcher.None.map(_ => IntMsg(10)), Watcher.None)
  }

  test("Run") {
    var state = 0

    val callback: Either[Throwable, Int] => Unit = {
      case Right(i) => state = i; ()
      case Left(_)  => throw new Exception("failed")
    }

    val observable: Obs[Int] = IO.delay { cb =>
      cb(Right(10))
      IO(Option(IO(())))
    }

    val runnable =
      Watcher.Observe("test", observable, i => Option(IntMsg(i)))

    runnable.run(callback).map(_ => state == 10).assert
  }

}
