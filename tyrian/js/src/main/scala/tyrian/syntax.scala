package tyrian

import cats.Applicative
import cats.effect.Async

object syntax:

  /** Make a side effect cmd from any `F[Unit]`
    */
  extension [F[_]](task: F[Unit])
    def toCmd: Cmd.SideEffect[F] =
      Cmd.SideEffect(task)

  /** Make a cmd from any `F[A]`
    */
  extension [F[_]: Applicative, A](task: F[A])
    def toCmd: Cmd.Run[F, A, A] =
      Cmd.Run[F, A](task)

  /** Make a sub from an `fs2.Stream`
    */
  extension [F[_]: Async, A](stream: fs2.Stream[F, A])
    def toSub(id: String): Sub[F, A] =
      Sub.make(id, stream)
