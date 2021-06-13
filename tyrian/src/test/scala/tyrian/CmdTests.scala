package tyrian

class CmdTests extends munit.FunSuite {

  test("mapping commands - Empty") {
    assertEquals(Cmd.Empty.map(_ => Int), Cmd.Empty)
  }

  test("mapping commands - RunTask") {
    val mapped =
      Cmd
        .RunTask[String, Int, Int](Task.Succeeded(10), (res: Either[String, Int]) => res.toOption.getOrElse(0))
        .map(_ * 100)

    val actual: Int =
      mapped.task match
        case Task.Succeeded(value) => mapped.f(Right(value))
        case _                     => throw new Exception("failed")

    val expected: Int =
      1000

    assertEquals(actual, expected)
  }

}
