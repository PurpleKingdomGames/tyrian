package tyrian.runtime

import cats.effect.unsafe.implicits.global
import tyrian.Cmd

object CmdRunner:

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def runCmd[Msg](
      cmd: Cmd[Msg],
      callback: Msg => Unit,
      async: (=> Unit) => Unit
  ): Unit =
    val allCmds = {
      def loop(cmd: Cmd[Msg]): Unit =
        cmd match
          case Cmd.Empty =>
            ()

          case Cmd.Emit(msg) =>
            callback(msg)

          case Cmd.SideEffect(task) =>
            async(task.unsafeRunAndForget())

          case Cmd.Run(obs, f) =>
            async(
              obs.unsafeRunAsync {
                case Right(v) => (f andThen callback)(v)
                case Left(e)  => throw e
              }
            )

          case Cmd.Combine(cmd1, cmd2) =>
            loop(cmd1)
            loop(cmd2)

          case Cmd.Batch(cmds) =>
            cmds.foreach(loop)

      loop(cmd)
    }
