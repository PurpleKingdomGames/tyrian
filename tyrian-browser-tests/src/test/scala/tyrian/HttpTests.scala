package tyrian

import cats.effect.IO
import io.circe.HCursor
import io.circe.parser.*
import tyrian.http.*

class HttpTests extends munit.CatsEffectSuite {

  type Msg = Either[HttpError, Response]
  private val msgDecoder = Decoder(Right(_), Left(_))

  private def getBodyData(response: Response): Option[String] =
    parse(response.body).toOption
      .flatMap(_.hcursor.get[String]("data").toOption)

  private def getBodyHeader(response: Response, header: String): Option[String] =
    parse(response.body).toOption
      .flatMap(_.hcursor.downField("headers").get[String](header).toOption)

  private def runCmd[Msg](cmd: Cmd[IO, Msg]): IO[Msg] =
    cmd match
      case c: Cmd.Run[IO, _, _] =>
        c.task.map(c.toMsg)

      case Cmd.Emit(msg) =>
        IO(msg)

      case _ =>
        IO.raiseError(new Exception("failed, was not a run task"))

  test("HEAD") {
    val result = runCmd(
      Http.send(
        Request(Method.Head, "http://httpbin.org/get"),
        msgDecoder
      )
    ).map(_.toOption.map(_.status))

    result.assertEquals(Some(Status(200, "OK")))
  }

  test("POST") {
    val result = runCmd(
      Http.send(
        Request(Method.Post, "http://httpbin.org/post", Body.plainText("stuff")),
        msgDecoder
      )
    ).map(_.toOption.flatMap(getBodyData))

    result.assertEquals(Some("stuff"))
  }

  test("PUT") {
    val result = runCmd(
      Http.send(
        Request(Method.Put, "http://httpbin.org/put", Body.plainText("stuff")),
        msgDecoder
      )
    ).map(_.toOption.flatMap(getBodyData))

    result.assertEquals(Some("stuff"))
  }

  test("PATCH") {
    val result = runCmd(
      Http.send(
        Request(Method.Patch, "http://httpbin.org/patch", Body.plainText("stuff")),
        msgDecoder
      )
    ).map(_.toOption.flatMap(getBodyData))

    result.assertEquals(Some("stuff"))
  }

  test("DELETE") {
    val result = runCmd(
      Http.send(
        Request(Method.Delete, "http://httpbin.org/delete", Body.plainText("stuff")),
        msgDecoder
      )
    ).map(_.toOption.flatMap(getBodyData))

    result.assertEquals(Some("stuff"))
  }

  test("GET with headers") {
    val result = runCmd(
      Http.send(
        Request(Method.Get, "http://httpbin.org/headers").addHeaders(Header("Test-Header", "123")),
        msgDecoder
      )
    ).map(_.toOption.flatMap(getBodyHeader(_, "Test-Header")))

    result
      .assertEquals(Some("123"))
  }
}
