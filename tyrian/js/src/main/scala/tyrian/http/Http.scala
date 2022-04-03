package tyrian.http

import cats.effect.IO
import org.scalajs.dom.XMLHttpRequest
import tyrian.Cmd

import scala.concurrent.Promise
import scala.util.Try

object Http:

  enum ConnectionResult:
    case Handler(xhr: XMLHttpRequest)
    case Error(e: HttpError)

  enum HttpResult[A]:
    case Success(value: A)
    case Failure(e: HttpError)

  /** Tries to transforms a response body of type String to a value of type A.
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
    * @param resultToMessage
    *   transforms a successful or failed response into a Msg
    * @param request
    *   the request
    * @tparam A
    *   type of the successfully decoded response
    * @tparam Msg
    *   a tyrian Msg
    * @return
    *   A Cmd that describes the HTTP request
    */
  @SuppressWarnings(Array("scalafix:DisableSyntax.null"))
  def send[A, Msg](request: Request[A], resultToMessage: HttpResult[A] => Msg): Cmd[Msg] =

    val task =
      IO.fromFuture(
        IO {
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
      ).flatMap {
        case e @ ConnectionResult.Error(_) => IO(e)
        case ConnectionResult.Handler(xhr) => IO(xhr).onCancel(IO(xhr.abort()))
      }.flatMap {
        case ConnectionResult.Error(e) =>
          IO(HttpResult.Failure(e))

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
              IO(HttpResult.Success(r))

            case Left(e) =>
              IO(
                HttpResult.Failure(
                  HttpError.DecodingFailure(e, response)
                )
              )
      }

    Cmd.Run(task, resultToMessage)

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
