package tyrian

import cats.effect.Async

object syntax:

  /** Make a side effect cmd from any `F[Unit]`
    */
  extension [F[_]](task: F[Unit])
    def toCmd: Cmd.SideEffect[F] =
      Cmd.SideEffect(task)

  /** Make a cmd from any `F[A]`
    */
  extension [F[_], A, Msg](task: F[A])
    def toCmd: Cmd.Run[F, A, A] =
      Cmd.Run(task)(identity)

  /** Make a sub from an `fs2.Stream`
    */
  extension [F[_]: Async, A](stream: fs2.Stream[F, A])
    def toSub(id: String): Sub[F, A] =
      Sub.make(id, stream)
