package com.example.example

import cats.effect.Sync
import cats.implicits.*
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.Header
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType

object Routes:

  def routes[F[_]: Sync](ssr: SSR[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "ssr" / in =>
        for {
          out  <- ssr.render(SSR.Input(in))
          resp <- Ok(out.toHtml, `Content-Type`(MediaType.text.html))
        } yield resp

      case GET -> Root / "ssr" =>
        for {
          out  <- ssr.render
          resp <- Ok(out.toHtml, `Content-Type`(MediaType.text.html))
        } yield resp
    }
