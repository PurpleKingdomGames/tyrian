package tyrian.next

import cats.effect.IO

@SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
class WatchTests extends munit.CatsEffectSuite {

  type Obs[A] = IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]

  final case class IntMsg(i: Int) extends GlobalMsg

  import ActionWatchUtils.*

  test("map - Empty") {
    assertEquals(Watch.None.map(_ => IntMsg(10)), Watch.None)
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
      Watch.Observe("test", observable, i => Option(IntMsg(i)))

    runnable.run(callback).map(_ => state == 10).assert
  }

}
