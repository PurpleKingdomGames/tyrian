package example

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Ref
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger
import org.http4s.server.websocket.WebSocketBuilder2

object SandboxSSR extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    def httpApp(wsb: WebSocketBuilder2[IO], gameState: Ref[IO, GameOfLife]) =
      Routes.routes[IO](CorvidDatabase.fakeImpl[IO], gameState, wsb).orNotFound

    for {
      gameState <- Ref[IO].of(GameOfLife.chaotic)
      result <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpWebSocketApp(wsb => Logger.httpApp(true, false)(httpApp(wsb, gameState)))
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield result
