package myorg

import cats.effect.IO
import org.scalajs.dom
import tyrian.Html.*
import tyrian.*

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("TyrianApp")
object HelloTyrian extends TyrianApp[Msg, Model]:

  /** Implements the standard router function using url-dsl:
    * https://github.com/sherpal/url-dsl
    */
  def router: Location => Msg =
    CustomRoutes.router

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
      p("Hello from the homepage!"),
      ul(
        li(a(href := Page.Counter.address)("Go to the counter")),
        li(a(href := Page.UserPage(1).address)("See user by id")),
        li(a(href := Page.UserAgePage(Option(23)).address)("See user by age"))
      )
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
      p("The requested page was not found..."),
      button(onClick(Msg.NavigateTo(Page.Home)))("Go to homepage")
    )

  def viewUserPage(userId: Int): Html[Msg] =
    div(
      p(s"Welcome to the user page with user id: ${userId}"),
      button(onClick(Msg.NavigateTo(Page.Home)))("Go to home")
    )

  def viewUserMaybeAgePage(ageOption: Option[Int]): Html[Msg] =
    ageOption
      .map { age =>
        div(p(s"$age years old, eh?"))
      }
      .getOrElse(div(p("You didn't provide an age :(")))

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
  case Home                  extends Page("/")
  case Counter               extends Page("/counter")
  case NotFound              extends Page("/not-found")
  case UserPage(userId: Int) extends Page(s"/id/$userId")
  case UserAgePage(ageOption: Option[Int])
      extends Page(s"/user${ageOption.map(a => s"?age=$a").getOrElse("")}")

enum Msg:
  case Increment
  case Decrement
  case Reset
  case NavigateTo(page: Page)
  case FollowExternalLink(href: String)

object CustomRoutes:

  import urldsl.errors.*
  import urldsl.language.*
  import urldsl.language.simpleErrorImpl.*
  import urldsl.vocabulary.*

  // To avoid conflicts with Tyrian
  import urldsl.language.simpleErrorImpl.param

  /** These are our routes. Since they don't change throughout the life of the
    * app, we can store them in a val for resuse.
    *
    * Route matching looks for the existance of the desired path within the
    * given path, meaning that we cannot match on 'root' alone for the home
    * page, because it matches everything. The route matcher takes care of this
    * later.
    */
  val routes: List[Route] =
    List(
      Route.Simple[Unit](
        root / "counter",
        _ => Page.Counter
      ),
      Route.Simple[Int](
        root / "id" / segment[Int],
        (userId: Int) => Page.UserPage(userId)
      ),
      Route.WithParam[Unit, Option[Int]](
        (root / "user" / endOfSegments) ? param[Int]("age").?,
        { case UrlMatching(_, ageOption) =>
          Page.UserAgePage(ageOption)
        }
      )
    )

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
        val page =
          CustomRoutes.routerFromList(
            path = loc.pathName,
            search = loc.search,
            notFound = Page.NotFound,
            routeList = routes
          )

        Msg.NavigateTo(page)

      case loc: Location.External =>
        Msg.FollowExternalLink(loc.href)

  /** First checks if the page requested is empty or "/", and gives the homepage
    * if true. Otherwise, works through the list of routes until it finds a
    * match, and produces a Page / Not Found result.
    */
  private def routerFromList(
      path: String,
      search: Option[String],
      notFound: Page,
      routeList: List[Route]
  ): Page =
    if path.isEmpty || path == "/" then Page.Home
    else
      routeList
        .find(_.matches(path, search))
        .flatMap(_.producePath(path, search))
        .getOrElse(notFound)

  /** Custom route type that knows how to use url-dsl to match routes.
    */
  enum Route:

    def matches(path: String, search: Option[String]): Boolean =
      this match
        case r: Route.Simple[_] =>
          r.pathSegment.matchPath(path).isRight

        case r: Route.WithParam[_, _] =>
          r.pathSegment.matchPathAndQuery(path, search.getOrElse("")).isRight

    def producePath(path: String, search: Option[String]): Option[Page] =
      this match
        case r: Route.Simple[_] =>
          r.pathSegment
            .matchPath(path)
            .toOption
            .map(r.matchPath)

        case r: Route.WithParam[_, _] =>
          r.pathSegment
            .matchPathAndQuery(path, search.getOrElse(""))
            .toOption
            .map(r.matchPath)

    case Simple[PathType](
        pathSegment: PathSegment[PathType, SimplePathMatchingError],
        matchPath: PathType => Page
    )

    case WithParam[PathType, ParamsType](
        pathSegment: PathSegmentWithQueryParams[
          PathType,
          SimplePathMatchingError,
          ParamsType,
          SimpleParamMatchingError
        ],
        matchPath: UrlMatching[PathType, ParamsType] => Page
    )
