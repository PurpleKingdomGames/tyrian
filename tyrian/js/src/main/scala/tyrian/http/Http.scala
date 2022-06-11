package tyrian.http

import cats.effect.kernel.Async
import org.scalajs.dom.XMLHttpRequest
import tyrian.Cmd

import scala.annotation.targetName
import scala.util.Try

/** Send HTTP requests as a command */
object Http:

  private enum Connection:
    case Handler(xhr: XMLHttpRequest)
    case Error(e: HttpError)

  /** Send an HTTP request.
    * @param request
    *   the request
    * @param resultToMessage
    *   transforms a successful or failed response into a Msg
    * @tparam F
    *   Effect type, e.g. `IO`
    * @tparam A
    *   type of the successfully decoded response
    * @tparam Msg
    *   a tyrian Msg
    * @return
    *   A Cmd that describes the HTTP request
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def send[F[_]: Async, A, Msg](request: Request[A], resultToMessage: Decoder[Msg]): Cmd[F, Msg] =
    val task: F[Connection] =
      Async[F].async_ { callback =>
        val xhr = new XMLHttpRequest

        try {
          xhr.timeout = request.timeout.toMillis.toDouble
          xhr.withCredentials = request.withCredentials
          xhr.open(request.method.asString, request.url)
          xhr.onload = _ => callback(Right(Connection.Handler(xhr)))
          xhr.onerror = _ => callback(Right(Connection.Error(HttpError.NetworkError)))
          xhr.ontimeout = _ => callback(Right(Connection.Error(HttpError.Timeout)))

          request.headers.foreach(h => xhr.setRequestHeader(h.name, h.value))
          request.body match
            case Body.Empty =>
              xhr.send(null)

            case Body.PlainText(contentType, body) =>
              xhr.setRequestHeader("Content-Type", contentType)
              xhr.send(body)

        } catch {
          case e: Throwable => callback(Left(e))
        }
      }

    val withResponse = Async[F].map(task) {
      case Connection.Error(e) =>
        resultToMessage.withError(e)

      case Connection.Handler(xhr) =>
        resultToMessage.withResponse(
          Response(
            url = request.url,
            status = Status(xhr.status, xhr.statusText),
            headers = parseHeaders(xhr.getAllResponseHeaders()),
            body = xhr.responseText
          )
        )
    }

    val withTimeout =
      Async[F].timeoutTo(
        withResponse,
        request.timeout,
        Async[F].delay(resultToMessage.withError(HttpError.Timeout))
      )

    Cmd.Run(withTimeout, identity)

  @SuppressWarnings(Array("scalafix:DisableSyntax.noValPatterns"))
  private def parseHeaders(headers: String): Map[String, String] =
    headers
      .split("[\\u000d\\u000a]+")
      .flatMap(h =>
        Try {
          val Array(fst, scd) = h.split(":").map(_.trim()).slice(0, 2)
          (fst, scd)
        }.toOption
      )
      .toMap
