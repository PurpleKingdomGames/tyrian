package tyrian

import cats.effect.IO

import scala.concurrent.duration.*

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class CmdTests extends munit.CatsEffectSuite {

  import CmdSubUtils.*

  test("cmd is a monoid") {
    val cmds = List(
      Cmd.Emit(10),
      Cmd.Emit(20),
      Cmd.Emit(30)
    )

    val actual =
      Cmd.combineAll(cmds)

    val expected =
      Cmd.Combine(
        Cmd.Combine(
          Cmd.Emit(10),
          Cmd.Emit(20)
        ),
        Cmd.Emit(30)
      )

    assertEquals(actual, expected)
  }

  test("map - Empty") {
    assertEquals(Cmd.None.map(_ => 10), Cmd.None)
  }

  test("Emit") {
    val cmd: Cmd[IO, Int] =
      Cmd.Emit(10).delayBy(1.seconds)

    val actual: IO[Int] =
      cmd.run

    actual.assertEquals(10)
  }

  test("map - Run") {
    val mapped =
      Cmd
        .Run(IO(10), (res: Int) => res.toString)
        .map(s => s"count: $s")

    val actual: IO[String] =
      mapped.run

    val expected: String =
      "count: 10"

    actual.assertEquals(expected)
  }

  test("map - Combine") {
    val mapped =
      Cmd.Combine(
        Cmd
          .Run(IO(10), identity)
          .map(_ * 100),
        Cmd
          .Run(IO(10), identity)
          .map(_ * 10)
      )

    mapped.cmd1.run.assertEquals(1000)
    mapped.cmd2.run.assertEquals(100)
  }

  test("map - Batch") {
    val mapped =
      Cmd.Batch(
        Cmd.Run(IO(10), identity).map(_ * 2),
        Cmd.Combine(
          Cmd.Run(IO(10), identity).map(_ * 100),
          Cmd.Run(IO(10), identity).map(_ * 10)
        )
      )

    val actual: IO[(Int, Int, Int)] =
      mapped.cmds match
        case c1 :: (cs: Cmd.Combine[IO, _]) :: Nil =>
          for {
            a <- runCmd(c1)
            b <- runCmd(cs.cmd1)
            c <- runCmd(cs.cmd2)
          } yield (a, b, c)

        case _ =>
          throw new Exception("wrong pattern")

    val expected: (Int, Int, Int) =
      (20, 1000, 100)

    actual.assertEquals(expected)
  }

  test("Combine to Batch") {
    val actual =
      Cmd
        .Combine(
          Cmd.Emit(10),
          Cmd.Emit(20)
        )
        .toBatch

    val expected =
      Cmd.Batch(
        Cmd.Emit(10),
        Cmd.Emit(20)
      )

    assertEquals(actual, expected)
  }

  test("Batch cons") {
    val actual =
      Cmd.Emit(10) :: Cmd.Batch(Cmd.Emit(20))

    val expected =
      Cmd.Batch(Cmd.Emit(10), Cmd.Emit(20))

    assertEquals(actual, expected)
  }

  test("Batch prepend") {
    val actual =
      Cmd.Emit(10) +: Cmd.Batch(Cmd.Emit(20))

    val expected =
      Cmd.Batch(Cmd.Emit(10), Cmd.Emit(20))

    assertEquals(actual, expected)
  }

  test("Batch append") {
    val actual =
      Cmd.Batch(Cmd.Emit(10)) :+ Cmd.Emit(20)

    val expected =
      Cmd.Batch(Cmd.Emit(10), Cmd.Emit(20))

    assertEquals(actual, expected)
  }

  test("Batch concat") {
    val actual =
      Cmd.Batch(Cmd.Emit(10), Cmd.Emit(20)) ++ Cmd.Batch(Cmd.Emit(30), Cmd.Emit(40))

    val expected =
      Cmd.Batch(Cmd.Emit(10), Cmd.Emit(20), Cmd.Emit(30), Cmd.Emit(40))

    assertEquals(actual, expected)
  }

}
