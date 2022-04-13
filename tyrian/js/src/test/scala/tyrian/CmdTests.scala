package tyrian

import cats.effect.IO

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class CmdTests extends munit.CatsEffectSuite {

  import CmdSubUtils.*

  test("map - Empty") {
    assertEquals(Cmd.Empty.map(_ => 10), Cmd.Empty)
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

}
