package example.models

import tyrian.http.*

final case class HttpDetails(
    method: Method,
    url: Option[String],
    body: String,
    response: Option[Response],
    error: Option[String],
    timeout: Double,
    credentials: RequestCredentials,
    headers: List[(String, String)],
    cache: RequestCache
)

object HttpDetails:
  val initial: HttpDetails =
    HttpDetails(
      Method.Get,
      Option("http://httpbin.org/get"),
      "",
      None,
      None,
      10000,
      RequestCredentials.SameOrigin,
      List(),
      RequestCache.Default
    )
