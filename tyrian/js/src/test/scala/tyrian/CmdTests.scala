package tyrian

@SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
class CmdTests extends munit.FunSuite {

  test("map - Empty") {
    assertEquals(Cmd.Empty.map(_ => Int), Cmd.Empty)
  }

  test("map - RunTask") {
    val mapped =
      Cmd
        .RunTask[String, Int, Int](Task.Succeeded(10), (res: Either[String, Int]) => res.toOption.getOrElse(0))
        .map(_ * 100)

    val actual: Int =
      runCmd(mapped)

    val expected: Int =
      1000

    assertEquals(actual, expected)
  }

  test("map - Combine") {
    val mapped =
      Cmd.Combine(
        Cmd
          .RunTask[String, Int, Int](Task.Succeeded(10), (res: Either[String, Int]) => res.toOption.getOrElse(0))
          .map(_ * 100),
        Cmd
          .RunTask[String, Int, Int](Task.Succeeded(10), (res: Either[String, Int]) => res.toOption.getOrElse(0))
          .map(_ * 10)
      )

    val actual: (Int, Int) =
      (runCmd(mapped.cmd1), runCmd(mapped.cmd2))

    val expected: (Int, Int) =
      (1000, 100)

    assertEquals(actual, expected)
  }

  test("map - Batch") {
    val toMessage = (res: Either[String, Int]) => res.toOption.getOrElse(0)

    val mapped =
      Cmd.Batch(
        Cmd
          .RunTask[String, Int, Int](Task.Succeeded(10), toMessage)
          .map(_ * 2),
        Cmd.Combine(
          Cmd
            .RunTask[String, Int, Int](Task.Succeeded(10), toMessage)
            .map(_ * 100),
          Cmd
            .RunTask[String, Int, Int](Task.Succeeded(10), toMessage)
            .map(_ * 10)
        )
      )

    val actual: (Int, Int, Int) =
      mapped.cmds match
        case c1 :: (cs: Cmd.Combine[_]) :: Nil =>
          (runCmd(c1), runCmd(cs.cmd1), runCmd(cs.cmd2))

        case _ =>
          throw new Exception("wrong pattern")

    val expected: (Int, Int, Int) =
      (20, 1000, 100)

    assertEquals(actual, expected)
  }

  def runCmd[Msg](cmd: Cmd[Msg]): Msg =
    cmd match
      case c: Cmd.RunTask[_, _, _] =>
        c.task match
          case Task.Succeeded(value) => c.toMessage(Right(value))
          case _                     => throw new Exception("failed on run task")

      case Cmd.Emit(msg) =>
        msg

      case _ =>
        throw new Exception("failed, was not a run task")

}
