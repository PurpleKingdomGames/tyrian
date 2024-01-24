package example

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dom.FetchClientBuilder
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.*

@JSExportTopLevel("TyrianApp")
object Main extends TyrianIOApp[Msg, Model]:

  def router: Location => Msg = Routing.none(Msg.NoOp)

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model.init, Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.UpdateRepo(r) =>
      (model.copy(repo = r), Cmd.None)

    case Msg.FetchStars =>
      (
        model.copy(stargazersResult = "fetching..."),
        Http4sHelper.fetchStars(model.repo)
      )

    case Msg.Stars(res) =>
      (model.copy(stargazersResult = res), Cmd.None)

    case Msg.NoOp =>
      (model, Cmd.None)

  def view(model: Model): Html[Msg] =
    div(
      div(
        p("How many stars?"),
        br,
        input(
          placeholder := "http4s/http4s",
          onInput(s => Msg.UpdateRepo(s))
        ),
        button(onClick(Msg.FetchStars))("Fetch")
      ),
      div(p(model.stargazersResult))
    )

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

final case class Model(repo: String, stargazersResult: String)
object Model:
  val init: Model =
    Model("http4s/http4s", "")

enum Msg:
  case FetchStars
  case UpdateRepo(repo: String)
  case Stars(result: String)
  case NoOp

object Http4sHelper:

  final private case class Repo(stargazers_count: Int)

  def fetchStars(repoName: String): Cmd[IO, Msg] =

    val client = FetchClientBuilder[IO].create

    val fetchRepo: IO[String] =
      client
        .expect[Repo](s"https://api.github.com/repos/$repoName")
        .attempt
        .map {
          case Right(Repo(stars)) => s"$stars ★"
          case Left(_)            => s"Not found :("
        }

    Cmd.Run(fetchRepo)(s => Msg.Stars(s))
