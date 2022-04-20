package tyrian

import cats.effect.IO

@SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
class SubTests extends munit.CatsEffectSuite {

  type Obs[A] = IO[(Either[Throwable, A] => Unit) => IO[Option[IO[Unit]]]]

  import CmdSubUtils.*

  test("sub is a monoid") {
    var state = 0

    val callback: Either[Throwable, Int] => Unit = {
      case Right(i) => state = i; ()
      case Left(_)  => throw new Exception("failed")
    }

    val observable: Int => Obs[Int] = i =>
      IO.delay { cb =>
        cb(Right(i))
        IO(Option(IO(())))
      }

    val subs = List(
      Sub.Observe[IO, Int]("sub1", observable(10)),
      Sub.Observe[IO, Int]("sub2", observable(20)),
      Sub.Observe[IO, Int]("sub3", observable(30))
    )

    val result =
      Sub.combineAll(subs)

    result match {
      case Sub.Combine(sub1, combo) =>
        IO.both(
          sub1.run(callback).map(_ => state == 10).assert,
          combo match {
            case Sub.Combine(sub2, sub3) =>
              IO.both(
                sub2.run(callback).map(_ => state == 20).assert,
                sub3.run(callback).map(_ => state == 30).assert
              )

            case _ => throw new Exception("failed")
          }
        )

      case _ => throw new Exception("failed")
    }
  }

  test("map - Empty") {
    assertEquals(Sub.Empty.map(_ => 10), Sub.Empty)
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
      Sub.Observe[IO, Int]("test", observable)

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
        IO(Option(IO(())))
      }

    val combined =
      Sub.Combine(
        Sub.Observe[IO, Int]("sub1", observable(1000)),
        Sub.Observe[IO, Int]("sub2", observable(100))
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
        IO(Option(IO(())))
      }

    val batched =
      Sub.Batch[IO, Int](
        Sub.Observe[IO, Int]("sub1", observable(10)),
        Sub.Combine(
          Sub.Observe[IO, Int]("sub2", observable(100)),
          Sub.Observe[IO, Int]("sub3", observable(1000))
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
