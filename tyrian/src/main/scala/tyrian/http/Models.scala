package tyrian.http

import scala.concurrent.duration.FiniteDuration

/** An Error will be returned if something goes wrong with an HTTP request. */
enum HttpError:
  /** A BadUrl means that the provide URL is not valid.
    * @param msg
    *   error message
    */
  case BadUrl(msg: String) extends HttpError

  /** A Timeout means that it took too long to get a response. */
  case Timeout extends HttpError

  /** A NetworkError means that there is a problem with the network. */
  case NetworkError extends HttpError

  /** A BadStatus means that the status code of the response indicates failure.
    * @param response
    *   the response
    */
  case BadStatus(response: Response[String]) extends HttpError

  /** A BadPayload means that the body of the response could not be parsed correctly.
    * @param decodingError
    *   debugging message that explains what went wrong
    * @param response
    *   the response
    */
  case BadPayload(decodingError: String, response: Response[String]) extends HttpError

/** An HTTP method */
enum Method:
  case Get, Post, Put, Patch, Delete

/** The body of a request */
enum Body:
  /** Represents an empty body e.g. for GET requests or POST request without any data.
    */
  case Empty extends Body

  /** Create a request body with a string.
    * @param contentType
    *   the content type of the body
    * @param body
    *   the content of the body
    */
  case PlainText(contentType: String, body: String) extends Body

/** A request header
  * @param name
  *   header field name
  * @param value
  *   header field value
  */
final case class Header(name: String, value: String)

/** Describes an HTTP request.
  * @param method
  *   GET, POST, PUT, PATCH or DELETE
  * @param headers
  *   a list of request headers
  * @param url
  *   the url
  * @param body
  *   the request body (EmptyBody or StringBody(contentType: String, body: String))
  * @param expect
  *   tries to transform a Response[String] to a value of type A
  * @param timeout
  *   an optional timeout
  * @param withCredentials
  *   indicates if the request is using credentials
  * @tparam A
  *   type of the successfully decoded response
  */
final case class Request[A](
    method: Method,
    headers: List[Header],
    url: String,
    body: Body,
    expect: Response[String] => Either[String, A],
    timeout: Option[FiniteDuration],
    withCredentials: Boolean
)

/** The response from an HTTP request.
  * @param url
  *   the url
  * @param status
  *   the status code
  * @param headers
  *   the response headers
  * @param body
  *   the response body
  * @tparam A
  *   type of the response body
  */
final case class Response[A](
    url: String,
    status: Status,
    headers: Map[String, String],
    body: A
)

/** The response status code
  * @param code
  *   the status code
  * @param message
  *   the status message
  */
final case class Status(code: Int, message: String)
