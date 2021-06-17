package tyrian.runtime

import tyrian.Cmd
import tyrian.Task

class CmdRunnerTests extends munit.FunSuite {

  test("run a cmd") {

    var output: Int = -1

    val cmd: Cmd[Int] =
      Cmd.RunTask[String, Int, Int](Task.Succeeded(10), (res: Either[String, Int]) => res.toOption.getOrElse(0))

    val callback: Int => Unit = (i: Int) => {
      output = i
      ()
    }
    val async: (=> Unit) => Unit = thing => thing

    val actual =
      CmdRunner.runCmd(cmd, callback, async)

    assertEquals(output, 10)
  }

  test("run a cmd side effect") {

    var output: Int = -1

    val cmd: Cmd[Int] =
      Task.SideEffect(() => output = 2).toCmd

    val async: (=> Unit) => Unit = thing => thing

    val actual =
      CmdRunner.runCmd(cmd, _ => (), async)

    assertEquals(output, 2)
  }

  test("emit a message") {

    val initial = "Hello, "

    var output: String = initial

    final case class TestMsg(message: String)

    val msg = "world!"

    val cmd: Cmd[TestMsg] =
      Cmd.Emit[TestMsg](TestMsg(msg))

    val callback: TestMsg => Unit = (msg: TestMsg) => {
      output = output + msg.message
      ()
    }
    val async: (=> Unit) => Unit = thing => thing

    val actual =
      CmdRunner.runCmd(cmd, callback, async)

    assertEquals(output, initial + msg)
  }

}
