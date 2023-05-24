package myorg

import cats.effect.IO
import org.scalajs.dom
import tyrian.Html.{param => _, _}
import tyrian.*
import urldsl.errors.SimpleParamMatchingError
import urldsl.errors.SimplePathMatchingError
import urldsl.language.PathSegment
import urldsl.language.PathSegmentWithQueryParams
import urldsl.language.simpleErrorImpl.*
import urldsl.vocabulary.Param
import urldsl.vocabulary.Segment
import urldsl.vocabulary.UrlMatching

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("TyrianApp")
object HelloTyrian extends TyrianApp[Msg, Model]:

  /** Implements the standard router function using url-dsl:
    * https://github.com/sherpal/url-dsl
    */
  def router: Location => Msg =
    Routes.router

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (Model(Page.Home, 0), Cmd.None)

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Increment =>
      (model.copy(counter = model.counter + 1), Cmd.None)

    case Msg.Decrement =>
      (model.copy(counter = model.counter - 1), Cmd.None)

    case Msg.Reset =>
      (model.copy(counter = 0), Cmd.None)

    case Msg.NavigateTo(page) =>
      (model.copy(page = page), Nav.pushUrl(page.address))

    case Msg.FollowExternalLink(href) =>
      (model, Nav.loadUrl(href))

  def viewHome(model: Model): Html[Msg] =
    div(
      p("Hello from home!"),
      button(onClick(Msg.NavigateTo(Page.Counter)))("Go counter")
    )

  def viewCounter(model: Model): Html[Msg] =
    div(`class` := "flex flex-col justify-center items-center")(
      p(`class` := "mb-2 justify-self-center")("Counter Application"),
      div(`class` := "flex flex-row")(
        button(
          `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2",
          onClick(Msg.Increment)
        )(
          "+"
        ),
        p(model.counter.toString()),
        button(
          `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded ml-2",
          onClick(Msg.Decrement)
        )("-")
      ),
      if (model.counter != 0)
        button(
          `class` := "bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded mt-2",
          onClick(Msg.Reset)
        )("Reset")
      else
        button(
          `class` := "bg-red-300 text-gray-700 font-bold py-2 px-4 rounded mt-2 cursor-not-allowed"
        )("Reset"),
      button(onClick(Msg.NavigateTo(Page.Home)))("Go to homepage")
    )

  def viewNotFound(): Html[Msg] =
    div(
      p("Not found the requested page"),
      button(onClick(Msg.NavigateTo(Page.Home)))("Go to homepage")
    )

  def viewUserPage(userId: Int): Html[Msg] =
    div(
      p(s"Welcome at user page with user id: ${userId}"),
      button(onClick(Msg.NavigateTo(Page.Home)))("Go to home")
    )

  def viewUserMaybeAgePage(ageOption: Option[Int]): Html[Msg] =
    ageOption.fold(div(p("You're so boring...")))(age =>
      div(p(s"Thanks, you are cool and have $age years!"))
    )

  def view(model: Model): Html[Msg] =
    model.page match
      case Page.Home             => viewHome(model)
      case Page.Counter          => viewCounter(model)
      case Page.NotFound         => viewNotFound()
      case Page.UserPage(userId) => viewUserPage(userId)
      case Page.UserAgePage(age) => viewUserMaybeAgePage(age)

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

final case class Model(page: Page, counter: Int)

enum Page(val address: String):
  case Home                  extends Page("/home")
  case Counter               extends Page("/counter")
  case NotFound              extends Page("/not-found")
  case UserPage(userId: Int) extends Page(s"/id/$userId")
  case UserAgePage(ageOption: Option[Int])
      extends Page(s"/user${ageOption.map(a => s"?age=$a").getOrElse("")}")

enum Msg:
  case Increment, Decrement, Reset
  case NavigateTo(page: Page)
  case FollowExternalLink(href: String)

object Routes:

  /** This is our routing function.
    *
    * Internal routing uses our url-dsl based router.
    *
    * External routing uses the standard Tyrian routing pattern, on the
    * assumption that we just want to follow all links.
    */
  def router: Location => Msg = loc =>
    loc match
      case loc: Location.Internal =>
        Msg.NavigateTo(
          Routes.routerFromList(
            path = loc.pathName,
            notFound = Page.NotFound,
            List(
              SimpleRoute[Unit](
                root / "home",
                _ => Page.Home
              ),
              SimpleRoute[Unit](
                root / "counter",
                _ => Page.Counter
              ),
              SimpleRoute[Int](
                root / "id" / segment[Int],
                (userId: Int) => Page.UserPage(userId)
              ),
              ComplexRoute[Unit, Option[Int]](
                (root / "user" / endOfSegments) ? param[Int]("age").?,
                { case UrlMatching(_, ageOption) =>
                  Page.UserAgePage(ageOption)
                }
              )
            )
          )
        )

      case loc: Location.External =>
        Msg.FollowExternalLink(loc.href)

  def routerFromList(
      path: String,
      notFound: Page,
      xs: List[SimpleRoute[?] | ComplexRoute[?, ?]]
  ): Page =
    xs match {
      case Nil => notFound
      case (head: SimpleRoute[?]) :: Nil =>
        head.pathSegment
          .matchRawUrl(path)
          .fold(_ => notFound, head.combinator(_))

      case (head: ComplexRoute[?, ?]) :: Nil =>
        head.pathSegment
          .matchRawUrl(path)
          .fold(_ => notFound, head.combinator(_))

      case (head: SimpleRoute[?]) :: tail =>
        head.pathSegment
          .matchRawUrl(path)
          .fold(
            _ => routerFromList(path, notFound, tail),
            head.combinator(_)
          )

      case (head: ComplexRoute[?, ?]) :: tail =>
        head.pathSegment
          .matchRawUrl(path)
          .fold(
            _ => routerFromList(path, notFound, tail),
            head.combinator(_)
          )
    }

  final case class SimpleRoute[PathType](
      pathSegment: PathSegment[PathType, SimplePathMatchingError],
      combinator: PathType => Page
  )

  final case class ComplexRoute[PathType, ParamsType](
      pathSegment: PathSegmentWithQueryParams[
        PathType,
        SimplePathMatchingError,
        ParamsType,
        SimpleParamMatchingError
      ],
      combinator: UrlMatching[PathType, ParamsType] => Page
  )
