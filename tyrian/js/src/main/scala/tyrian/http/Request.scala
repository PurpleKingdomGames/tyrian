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
  *   duration to wait before giving up.
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
    timeout: FiniteDuration,
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

  def withTimeout(newTimeout: FiniteDuration): Request[A] =
    this.copy(timeout = newTimeout)

  def usingCredentials: Request[A] =
    this.copy(withCredentials = true)
  def noCredentials: Request[A] =
    this.copy(withCredentials = false)

object Request:
  val DefaultTimeOut: FiniteDuration = 10.seconds

  def apply[A](method: Method, url: String): Request[A] =
    Request[A](
      method,
      Nil,
      url,
      Body.Empty,
      DefaultTimeOut,
      false
    )

  def apply[A](method: Method, url: String, timeout: FiniteDuration): Request[A] =
    Request[A](
      method,
      Nil,
      url,
      Body.Empty,
      timeout,
      false
    )

  /** Convenience method to create a GET Request[A] and try to decode the response body from String to A.
    * @param url
    *   the url
    * @tparam A
    *   the type of the successfully decoded response
    * @return
    *   a GET request
    */
  def get[A](url: String): Request[A] =
    Request(
      method = Method.Get,
      headers = Nil,
      url = url,
      body = Body.Empty,
      timeout = DefaultTimeOut,
      withCredentials = false
    )

  /** Convenience method to create a POST Request[A] and try to decode the response body from String to A.
    * @param url
    *   the url
    * @param body
    *   the body of the POST request
    * @tparam A
    *   the type of the successfully decoded response
    * @return
    *   a POST request
    */
  def post[A](url: String, body: Body): Request[A] =
    Request(
      method = Method.Post,
      headers = Nil,
      url = url,
      body = body,
      timeout = DefaultTimeOut,
      withCredentials = false
    )
