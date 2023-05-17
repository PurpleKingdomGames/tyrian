package tyrian

import cats.effect.IO
import tyrian.syntax.*

import scala.concurrent.duration.*

@SuppressWarnings(Array("scalafix:DisableSyntax.throw", "scalafix:DisableSyntax.var"))
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
        case c1 :: (Cmd.Combine(cmd1, cmd2)) :: Nil =>
          for {
            a <- runCmd(c1)
            b <- runCmd(cmd1)
            c <- runCmd(cmd2)
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

  test("emitAfterDelay") {
    val cmd: Cmd[IO, Int] =
      Cmd.emitAfterDelay(10, 1.seconds)

    val actual: IO[Int] =
      cmd.run

    actual.assertEquals(10)
  }

  test("Cmd.Emit toTask") {
    val actual: IO[Int] =
      Cmd.Emit(10).toTask

    actual.assertEquals(10)
  }

  test("Cmd.SideEffect toTask") {
    var actual = 0

    val t = IO {
      actual = actual + 1
    }

    Cmd.SideEffect(t).toTask.map(_ => actual == 1).assert
  }

  test("Cmd.Run toTask") {
    Cmd.Run(IO(10), _.toString).toTask.assertEquals("10")
  }

  test("Cmd -> toTask -> Cmd") {
    var acc = 0

    val t: IO[String] =
      for {
        i <- Cmd.Emit(10).toTask[IO]
        _ <- IO { acc = acc + 1 }
        s <- IO((i + acc).toString)
      } yield s

    val actual =
      Cmd.Run(t, s => "count: " + s)

    actual.run.assertEquals("count: 11")
  }

  test("Cmd.Run produces message directly") {
    type Msg = String

    Cmd.Run[IO, Msg](IO("testing")).toTask.assertEquals("testing")
  }

  test("Cmd.Run use syntax to produce a command") {
    type Msg = String

    IO("testing 2").toCmd.toTask.assertEquals("testing 2")
  }

}
