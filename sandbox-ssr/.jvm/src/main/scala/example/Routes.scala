package example

import cats.effect.Sync
import cats.implicits.*
import fs2.io.file.Files
import org.http4s.Header
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.headers.`Content-Type`

object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")

object Routes:

  def routes[F[_]: Sync: Files](db: CorvidDatabase[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          corvids <- db.listAll
          resp <- Ok(
            HomePage.page(corvids).toString,
            `Content-Type`(MediaType.text.html)
          )
        } yield resp

      case GET -> Root / "search" :? SearchQueryParamMatcher(query) =>
        for {
          corvids <- db.search(query)
          htmlString = HomePage
            .listToHtmlTableRows(corvids)
            .mkString("\n")
          resp <- Ok(
            htmlString,
            `Content-Type`(MediaType.text.html)
          )
        } yield resp
    }
