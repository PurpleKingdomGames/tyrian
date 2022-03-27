// package tyrian

// object CmdUtils:

//   extension [A] (cmd: Cmd[A])
//     def run: A = runCmd(cmd)

//   @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
//   def runCmd[Msg](cmd: Cmd[Msg]): Msg =
//     cmd match
//       case c: Cmd.RunTask[_, _, _] =>
//         c.task match
//           case Task.Succeeded(value) => c.toMessage(Right(value))
//           case _                     => throw new Exception("failed on run task")

//       case Cmd.Emit(msg) =>
//         msg

//       case _ =>
//         throw new Exception("failed, was not a run task")
