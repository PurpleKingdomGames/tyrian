package tyrian.runtime

import tyrian.Cmd
import tyrian.Task

class CmdRunnerTests extends munit.FunSuite {

  test("run a cmd with a task") {

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

  test("run a cmd with an observable") {

    val toMessage = (res: Either[String, Int]) => res.toOption.getOrElse(throw new Exception(res.toString))

    var output: Int = -1

    val cmd: Cmd[Int] =
      Cmd.Run[String, Int] { observable =>
        observable.onNext(20)
        () => ()
      }.attempt(toMessage)

    val callback: Int => Unit = (i: Int) => {
      output = i
      ()
    }
    val async: (=> Unit) => Unit = thing => thing

    val actual =
      CmdRunner.runCmd(cmd, callback, async)

    assertEquals(output, 20)
  }

  test("run a cmd side effect") {

    var output: Int = -1

    val cmd: Cmd[Int] =
      Cmd.SideEffect(() => output = 2)

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
