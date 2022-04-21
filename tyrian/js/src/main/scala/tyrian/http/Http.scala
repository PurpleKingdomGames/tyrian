package tyrian.http

import cats.effect.kernel.Async
import org.scalajs.dom.XMLHttpRequest
import tyrian.Cmd

import scala.concurrent.Promise
import scala.util.Try

/** Send HTTP requests as a command */
object Http:

  enum ConnectionResult:
    case Handler(xhr: XMLHttpRequest)
    case Error(e: HttpError)

  enum HttpResult[A]:
    case Success(value: A)
    case Failure(e: HttpError)

  /** Tries to transform an response body of type String to a value of type A.
    * @tparam A
    *   type of the successfully decoded response
    */
  opaque type Decoder[A] = Response[String] => Either[String, A]
  object Decoder:
    def apply[A](decoder: Response[String] => Either[String, A]): Decoder[A] = decoder

    val asString: Decoder[String] =
      Decoder[String](response => Right(response.body))

    extension [A](d: Decoder[A])
      def parse(response: Response[String]): Either[String, A] =
        d(response)

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
  def send[F[_]: Async, A, Msg](request: Request[A], resultToMessage: HttpResult[A] => Msg): Cmd[F, Msg] =

    val task =
      Async[F].fromFuture(
        Async[F].delay {
          val xhr = new XMLHttpRequest
          val p   = Promise[ConnectionResult]()
          try {
            request.headers.foreach(h => xhr.setRequestHeader(h.name, h.value))

            xhr.timeout = request.timeout.map(_.toMillis.toDouble).getOrElse(0)
            xhr.withCredentials = request.withCredentials
            xhr.open(request.method.asString, request.url)
            xhr.onload = _ => p.success(ConnectionResult.Handler(xhr))
            xhr.onerror = _ => p.success(ConnectionResult.Error(HttpError.NetworkError))
            xhr.ontimeout = _ => p.success(ConnectionResult.Error(HttpError.Timeout))

            request.body match
              case Body.Empty =>
                xhr.send(null)

              case Body.PlainText(contentType, body) =>
                xhr.setRequestHeader("Content-Type", contentType)
                xhr.send(body)

          } catch {
            case ex: Throwable => p.success(ConnectionResult.Error(HttpError.BadRequest(ex.getMessage)))
          }
          p.future
        }
      )

    val withCancel = Async[F].flatMap(task) {
      case e @ ConnectionResult.Error(_) => Async[F].delay(e)
      case ConnectionResult.Handler(xhr) => Async[F].onCancel(Async[F].delay(xhr), Async[F].delay(xhr.abort()))
    }

    val withResponse = Async[F].flatMap(withCancel) {
      case ConnectionResult.Error(e) =>
        Async[F].delay(HttpResult.Failure(e))

      case ConnectionResult.Handler(xhr) =>
        val response = Response(
          url = request.url,
          status = Status(xhr.status, xhr.statusText),
          headers = parseHeaders(xhr.getAllResponseHeaders()),
          body = xhr.responseText
        )

        request
          .expect(response) match
          case Right(r) =>
            Async[F].delay(HttpResult.Success(r))

          case Left(e) =>
            Async[F].delay(
              HttpResult.Failure(
                HttpError.DecodingFailure(e, response)
              )
            )
    }

    Cmd.Run(withResponse, resultToMessage)

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
