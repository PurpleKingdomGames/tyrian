package tyrian

import cats.effect.IO
import tyrian.http.*

class FirefoxTests extends munit.CatsEffectSuite {

  type Msg = Either[HttpError, Response]
  private val msgDecoder = Decoder(Right(_), Left(_))

  private def runCmd[Msg](cmd: Cmd[IO, Msg]): IO[Msg] =
    cmd match
      case c: Cmd.Run[IO, ?, ?] =>
        c.task.map(c.toMsg)

      case Cmd.Emit(msg) =>
        IO(msg)

      case _ =>
        IO.raiseError(new Exception("failed, was not a run task"))

  test("NetworkError") {
    val result = runCmd(
      Http.send(
        Request.get("http://whatever:1234"),
        msgDecoder
      )
    )
    result
      .assertEquals(Left(HttpError.NetworkError))
  }
}
