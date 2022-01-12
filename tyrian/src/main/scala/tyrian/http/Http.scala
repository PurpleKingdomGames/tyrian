package tyrian
package http

import org.scalajs.dom.XMLHttpRequest

import scala.util.Try

object Http:

  /** Tries to transforms a response body of type String to a value of type A.
    * @tparam A
    *   type of the successfully decoded response
    */
  opaque type Decoder[A] = Response[String] => Either[String, A]
  object Decoder:
    def apply[A](decoder: Response[String] => Either[String, A]): Decoder[A] = decoder

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
  def send[A, Msg](request: Request[A], resultToMessage: Either[http.HttpError, A] => Msg): Cmd[Msg] =
    Task
      .RunObservable[http.HttpError, XMLHttpRequest] { observer =>
        val xhr = new XMLHttpRequest
        try {
          request.headers.foreach(h => xhr.setRequestHeader(h.name, h.value))

          xhr.timeout = request.timeout.map(_.toMillis.toDouble).getOrElse(0)
          xhr.withCredentials = request.withCredentials
          xhr.open(request.method.asString, request.url)
          xhr.onload = _ => observer.onNext(xhr)
          xhr.onerror = _ => observer.onError(HttpError.NetworkError)
          xhr.ontimeout = _ => observer.onError(HttpError.Timeout)

          request.body match
            case Body.Empty =>
              xhr.send(null)

            case Body.PlainText(contentType, body) =>
              xhr.setRequestHeader("Content-Type", contentType)
              xhr.send(body)

        } catch {
          case ex: Throwable => observer.onError(HttpError.BadRequest(ex.getMessage))
        }

        () => xhr.abort()
      }
      .attempt(_.flatMap { xhr =>
        val response = Response(
          url = request.url,
          status = Status(xhr.status, xhr.statusText),
          headers = parseHeaders(xhr.getAllResponseHeaders()),
          body = xhr.responseText
        )

        request
          .expect(response) match
          case Right(r) =>
            Right(r)

          case Left(e) =>
            Left(HttpError.DecodingFailure(e, response))
      })
      .map(resultToMessage)

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
