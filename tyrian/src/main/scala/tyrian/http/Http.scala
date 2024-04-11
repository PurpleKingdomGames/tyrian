package tyrian.http

import cats.effect.kernel.Async
import cats.implicits.*
import org.scalajs.dom
import org.scalajs.dom.HttpMethod
import org.scalajs.dom.RequestInit
import org.scalajs.dom.fetch
import tyrian.Cmd

import scala.annotation.nowarn
import scala.scalajs.js
import scala.util.Try

/** Send HTTP requests as a command */
object Http:

  case object TimeoutException                      extends Throwable
  case object NetworkErrorException                 extends Throwable
  case class UnknownErrorException(message: String) extends Throwable

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
  def send[F[_]: Async, A, Msg](request: Request[A], resultToMessage: Decoder[Msg]): Cmd.Run[F, Msg, Msg] =

    @nowarn("msg=unused")
    def fetchTask(abortController: dom.AbortController): F[dom.Response] = Async[F].async_ { callback =>

      val requestInit = new RequestInit {}
      val headers     = new dom.Headers()

      requestInit.method = request.method.asString.asInstanceOf[HttpMethod]
      requestInit.credentials = request.credentials.asString.asInstanceOf[dom.RequestCredentials]
      requestInit.signal = abortController.signal
      requestInit.cache = request.cache.asString.asInstanceOf[dom.RequestCache]
      request.headers.foreach(h => headers.append(h.name, h.value))

      (request.body, request.method) match
        case (Body.PlainText(contentType, body), method) if !Set(Method.Get, Method.Head).contains(method) =>
          headers.append("Content-Type", contentType)
          requestInit.body = body
        case _ =>

      requestInit.headers = headers

      fetch(request.url, requestInit)
        .`then`(response => callback(Right(response)))
        .`catch`(error => callback(Left(errorToThrowable(error))))

      ()
    }

    @nowarn("msg=unused")
    def textBodyTask(domResponse: dom.Response): F[String] = Async[F].async_ { callback =>
      domResponse
        .text()
        .`then`(text => callback(Right(text)))
        .`catch`(error => callback(Left(errorToThrowable(error))))

      ()
    }

    val fullTask = (for {
      abortController <- Async[F].delay(new dom.AbortController())
      domResponse <- Async[F].timeoutTo(
        fetchTask(abortController),
        request.timeout,
        Async[F].delay(abortController.abort()) >> Async[F].raiseError(TimeoutException)
      )
      textBody <- textBodyTask(domResponse)
    } yield resultToMessage.withResponse(
      Response(
        url = request.url,
        status = Status(domResponse.status, domResponse.statusText),
        headers = parseHeaders(domResponse.headers),
        body = textBody
      )
    )).recover {
      case _: TimeoutException.type =>
        resultToMessage.withError(HttpError.Timeout)
      case _: NetworkErrorException.type =>
        resultToMessage.withError(HttpError.NetworkError)
      case UnknownErrorException(message) =>
        resultToMessage.withError(HttpError.BadRequest(message))
    }

    Cmd.Run(fullTask, identity)

  @SuppressWarnings(Array("scalafix:DisableSyntax.noValPatterns"))
  private def parseHeaders(headers: dom.Headers): Map[String, String] =
    headers
      .map(_.toString())
      .flatMap(h =>
        Try {
          val Array(fst, scd) = h.split(",").map(_.trim()).slice(0, 2)
          (fst, scd)
        }.toOption
      )
      .toMap

  private def errorToThrowable(error: Any): Throwable =
    error match {
      case e: js.Error if e.name == "NetworkError"                                      => NetworkErrorException
      case e: js.Error if e.name == "TypeError" && e.message.startsWith("NetworkError") => NetworkErrorException
      case e: js.Error => UnknownErrorException(e.message)
      case e           => UnknownErrorException(e.toString())
    }
