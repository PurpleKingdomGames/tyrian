package example

import cats.effect.Async
import cats.effect.Ref
import cats.implicits.*
import fs2.Pipe
import fs2.Stream
import org.http4s.Header
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import org.http4s.headers.`Content-Type`
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.*

import concurrent.duration.DurationInt

object SearchQueryParamMatcher extends QueryParamDecoderMatcher[String]("q")

object ScrollQueryParamMatcher extends QueryParamDecoderMatcher[Long]("page")

object Routes:

  def routes[F[_]: Async](
      db: CorvidDatabase[F],
      gameState: Ref[F, GameOfLife],
      wsb: WebSocketBuilder2[F]
  ): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {

      case GET -> Root =>
        for {
          resp <- Ok(
            TabbedPage.page.toString,
            `Content-Type`(MediaType.text.html)
          )
        } yield resp

      case GET -> Root / "tab" / IntVar(tab) =>
        val tabs = TabbedPage.tabs(TabbedPage.randomTabs, tab)
        for {
          content <- tab match {
            case 0 =>
              db.listAll
                .map(SearchPage.page)
            case 1 =>
              InfiniteScroll.page
            case 2 =>
              WebSocketsPage.page.pure[F]
            case _: Int =>
              TabbedPage.emptyContent.pure[F]
          }
          resp <- Ok(
            List(tabs, content).mkString("\n"),
            `Content-Type`(MediaType.text.html)
          )
        } yield resp

      case GET -> Root / "search" :? SearchQueryParamMatcher(query) =>
        for {
          corvids <- db.search(query)
          htmlString = SearchPage
            .listToHtmlTableRows(corvids)
            .mkString("\n")
          resp <- Ok(
            htmlString,
            `Content-Type`(MediaType.text.html)
          )
        } yield resp

      case GET -> Root / "scroll" :? ScrollQueryParamMatcher(page) =>
        for {
          page <- InfiniteScroll.rowBatch(20L, page * 20L)
          resp <- Ok(
            page.mkString("\n"),
            `Content-Type`(MediaType.text.html)
          )
        } yield resp

      case GET -> Root / "ws" =>
        val receive: Pipe[F, WebSocketFrame, Unit] = _.evalMap { _ =>
          gameState.update(_ => GameOfLife.chaotic)
        }
        val send: Stream[F, WebSocketFrame] =
          Stream
            .awakeEvery(200.millis)
            .evalMap { _ =>
              gameState.updateAndGet(_.advance())
            }
            .map { game =>
              Text(WebSocketsPage.drawGameOfLife(game).toString())
            }
        wsb.build(send, receive)
    }
