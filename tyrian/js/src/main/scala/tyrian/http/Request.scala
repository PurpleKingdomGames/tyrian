package tyrian.http

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

/** Describes an HTTP request.
  * @param method
  *   GET, POST, PUT, PATCH, DELETE, or OPTIONS
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
):
  def withMethod(newMethod: Method): Request[A] =
    this.copy(method = newMethod)

  def withHeaders(newHeaders: List[Header]): Request[A] =
    this.copy(headers = newHeaders)
  def withHeaders(newHeaders: Header*): Request[A] =
    withHeaders(newHeaders.toList)
  def addHeaders(newHeaders: List[Header]): Request[A] =
    this.copy(headers = headers ++ newHeaders)
  def addHeaders(newHeaders: Header*): Request[A] =
    addHeaders(newHeaders.toList)

  def withUrl(newUrl: String): Request[A] =
    this.copy(url = newUrl)

  def withBody(newBody: Body): Request[A] =
    this.copy(body = newBody)

  def withExpectation(newExpectation: Response[String] => Either[String, A]): Request[A] =
    this.copy(expect = newExpectation)

  def withTimeout(newTimeout: FiniteDuration): Request[A] =
    this.copy(timeout = Option(newTimeout))

  def usingCredentials: Request[A] =
    this.copy(withCredentials = true)
  def noCredentials: Request[A] =
    this.copy(withCredentials = false)

object Request:
  def apply[A](method: Method, url: String, expect: Response[String] => Either[String, A]): Request[A] =
    Request[A](
      method,
      Nil,
      url,
      Body.Empty,
      expect,
      Some(5.seconds),
      false
    )

  /** Convenience method to create a GET Request[A] and try to decode the response body from String to A.
    * @param url
    *   the url
    * @param decoder
    *   tries to transform the body into some value of type A
    * @tparam A
    *   the type of the successfully decoded response
    * @return
    *   a GET request
    */
  def get[A](url: String, decoder: Http.Decoder[A]): Request[A] =
    Request(
      method = Method.Get,
      headers = Nil,
      url = url,
      body = Body.Empty,
      expect = r => decoder.parse(r),
      timeout = None,
      withCredentials = false
    )

  /** Convenience method to create a GET Request[A] and interpret the response body as String.
    * @param url
    *   the url
    * @return
    *   a GET request
    */
  def get(url: String): Request[String] =
    get(url, Http.Decoder(response => Right(response.body)))

  /** Convenience method to create a POST Request[A] and try to decode the response body from String to A.
    * @param url
    *   the url
    * @param body
    *   the body of the POST request
    * @param decoder
    *   tries to transform the body into some value of type A
    * @tparam A
    *   the type of the successfully decoded response
    * @return
    *   a POST request
    */
  def post[A](url: String, body: Body, decoder: Http.Decoder[A]): Request[A] =
    Request(
      method = Method.Post,
      headers = Nil,
      url = url,
      body = body,
      expect = r => decoder.parse(r),
      timeout = None,
      withCredentials = false
    )

  /** Convenience method to create a POST Request[A] and interpret the response body as String.
    * @param url
    *   the url
    * @param body
    *   the body of the POST request
    * @param decoder
    *   tries to transform the body into some value of type A
    * @tparam A
    *   the type of the successfully decoded response
    * @return
    *   a POST request
    */
  def post(url: String, body: Body): Request[String] =
    post(url, body, Http.Decoder(response => Right(response.body)))
