package tyrian

import cats.effect.IO

@SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
class SubTests extends munit.CatsEffectSuite {

  type Obs[A] = IO[(Either[Throwable, A] => Unit) => IO[Unit]]

  import CmdSubUtils.*

  test("map - Empty") {
    assertEquals(Sub.Empty[IO]().map(_ => 10), Sub.Empty[IO]())
  }

  test("Run") {
    var state = 0

    val callback: Either[Throwable, Int] => Unit = {
      case Right(i) => state = i; ()
      case Left(_)  => throw new Exception("failed")
    }

    val observable: Obs[Int] = IO.delay { cb =>
      cb(Right(10))
      IO(())
    }

    val runnable =
      Sub.OfObservable[IO, Int, Int]("test", observable, identity)

    runnable.run(callback).map(_ => state == 10).assert
  }

  test("Combine") {
    var state = 0

    val callback: Either[Throwable, Int] => Unit = {
      case Right(i) => state = i; ()
      case Left(_)  => throw new Exception("failed")
    }

    val observable: Int => Obs[Int] = i =>
      IO.delay { cb =>
        cb(Right(i))
        IO(())
      }

    val combined =
      Sub.Combine(
        Sub.OfObservable[IO, Int, Int]("sub1", observable(1000), identity),
        Sub.OfObservable[IO, Int, Int]("sub2", observable(100), identity)
      )

    IO.both(
      combined.sub1.run(callback).map(_ => state == 1000).assert,
      combined.sub2.run(callback).map(_ => state == 100).assert
    )
  }

  test("map - Batch") {
    var state = 0

    val callback: Either[Throwable, Int] => Unit = {
      case Right(i) => state = i; ()
      case Left(_)  => throw new Exception("failed")
    }

    val observable: Int => Obs[Int] = i =>
      IO.delay { cb =>
        cb(Right(i))
        IO(())
      }

    val batched =
      Sub.Batch[IO, Int](
        Sub.OfObservable[IO, Int, Int]("sub1", observable(10), identity),
        Sub.Combine(
          Sub.OfObservable[IO, Int, Int]("sub2", observable(100), identity),
          Sub.OfObservable[IO, Int, Int]("sub3", observable(1000), identity)
        )
      )

    batched.subs match
      case s1 :: (ss: Sub.Combine[IO, _]) :: Nil =>
        IO.both(
          s1.run(callback).map(_ => state == 10).assert,
          IO.both(
            ss.sub1.run(callback).map(_ => state == 100).assert,
            ss.sub2.run(callback).map(_ => state == 1000).assert
          )
        )

      case _ =>
        throw new Exception("wrong pattern")
  }

}
