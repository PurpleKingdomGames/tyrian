package tyrian.http

/** An Error will be returned if something goes wrong with an HTTP request. */
enum HttpError:
  /** A BadRequest means that the provide request was not valid for some reason.
    * @param msg
    *   error message
    */
  case BadRequest(msg: String) extends HttpError

  /** A Timeout means that it took too long to get a response. */
  case Timeout extends HttpError

  /** A NetworkError means that there is a problem with the network. */
  case NetworkError extends HttpError

  /** A BadPayload means that the body of the response could not be parsed correctly.
    * @param decodingError
    *   debugging message that explains what went wrong
    * @param response
    *   the response
    */
  case DecodingFailure(decodingError: String, response: Response[String]) extends HttpError

/** An HTTP method */
enum Method derives CanEqual:
  case Get, Post, Put, Patch, Delete, Options

  def asString: String =
    this match
      case Get     => "GET"
      case Post    => "POST"
      case Put     => "PUT"
      case Patch   => "PATCH"
      case Delete  => "DELETE"
      case Options => "OPTIONS"

/** The body of a request */
enum Body derives CanEqual:
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

  def html(body: String): Body = Body.PlainText("text/html", body)
  def json(body: String): Body = Body.PlainText("application/json", body)
  def xml(body: String): Body  = Body.PlainText("application/xml", body)

/** A request header
  * @param name
  *   header field name
  * @param value
  *   header field value
  */
final case class Header(name: String, value: String)

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
