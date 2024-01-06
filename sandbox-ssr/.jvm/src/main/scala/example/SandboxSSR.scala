package example

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

object SandboxSSR extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    val httpApp      = Routes.routes[IO](CorvidDatabase.fakeImpl[IO]).orNotFound
    val finalHttpApp = Logger.httpApp(true, false)(httpApp)
    EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalHttpApp)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
