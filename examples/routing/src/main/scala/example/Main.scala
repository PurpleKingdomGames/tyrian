package myorg

import cats.effect.IO
import myorg.Page.getPage
import org.scalajs.dom
import tyrian.Html.{param => _, _}
import tyrian.*
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

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (
      AppState(
        dom.window.location.href.getPage(),
        counter = 0
      ),
      Cmd.None
    )

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Increment => (model.copy(counter = model.counter + 1), Cmd.None)
    case Msg.Decrement => (model.copy(counter = model.counter - 1), Cmd.None)
    case Msg.Reset     => (model.copy(counter = 0), Cmd.None)
    case Msg.Void      => (model, Cmd.None)
    case Msg.GoToCounterPage =>
      val newModel = model.copy(page = Page.Counter)

      (
        newModel,
        Cmd.SideEffect(
          dom.window.history
            .replaceState({}, "Counter", newModel.page.asString())
        )
      )
    case Msg.GoToHomePage =>
      val newModel = model.copy(page = Page.Home)

      (
        newModel,
        Cmd.SideEffect(
          dom.window.history
            .replaceState({}, "Home", newModel.page.asString())
        )
      )

  def viewHome(model: Model): Html[Msg] =
    div(
      p("Hello from home!"),
      button(onClick(Msg.GoToCounterPage))("Go counter")
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
      button(onClick(Msg.GoToHomePage))("Go to homepage")
    )

  def viewNotFound(): Html[Msg] =
    div(
      p("Not found the requested page"),
      button(onClick(Msg.GoToHomePage))("Go to homepage")
    )

  def viewUserPage(userId: Int): Html[Msg] =
    div(
      p(s"Welcome at user page with user id: ${userId}"),
      button(onClick(Msg.GoToHomePage))("Go to home")
    )

  def viewUserMaybeAgePage(ageOption: Option[Int]): Html[Msg] =
    ageOption.fold(div(p("You're so boring...")))(age =>
      div(p(s"Thanks, you are cool and have $age years!"))
    )


  def view(model: Model): Html[Msg] =
    model.page match {
      case Page.Home             => viewHome(model)
      case Page.Counter          => viewCounter(model)
      case Page.NotFound         => viewNotFound()
      case Page.UserPage(userId) => viewUserPage(userId)
      case Page.UserAgePage(age) => viewUserMaybeAgePage(age)
    }

  def subscriptions(model: Model): Sub[IO, Msg] =
    Sub.None

enum Page {
  case Home
  case Counter
  case NotFound
  case UserPage(userId: Int)
  case UserAgePage(ageOption: Option[Int])

}

object Page {
  val homePath        = root / "home"
  val counterPath     = root / "counter"
  val moreComplexPath = root / "id" / segment[Int]
  val pathWithParam   = (root / "user" / endOfSegments) ? param[Int]("age").?

  case class SimpleRoute[X, P](
      pathSegment: PathSegment[X, SimplePathMatchingError],
      combinator: X => P
  )

  case class ComplexRoute[X, P](
      pathSegment: PathSegmentWithQueryParams[?, SimplePathMatchingError, X, ?],
      combinator: UrlMatching[?, X] => P
  )

  def routerFromList(
      xs: List[SimpleRoute[?, Page] | ComplexRoute[?, Page]],
      path: String
  ): Page =
    xs match {
      case Nil => Page.NotFound
      case (head: SimpleRoute[?, Page]) :: Nil =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => Page.NotFound, head.combinator(_))
      case (head: ComplexRoute[?, Page]) :: Nil =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => Page.NotFound, head.combinator(_))
      case (head: SimpleRoute[?, Page]) :: tail =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => routerFromList(tail, path), head.combinator(_))
      case (head: ComplexRoute[?, Page]) :: tail =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => routerFromList(tail, path), head.combinator(_))
    }

  extension (p: Page)
    def asString(): String = p match {
      case Counter          => "/counter"
      case Home             => "/home"
      case NotFound         => "/notfound"
      case UserPage(userId) => s"/id/${userId}"
      case UserAgePage(ageOption) =>
        ageOption match {
          case None      => "/user"
          case Some(age) => s"/user?age=${age}"
        }
    }

  extension (path: String)
    def getPage(): Page = routerFromList(
      List(
        SimpleRoute[Unit, Page](homePath, _ => Page.Home),
        SimpleRoute[Unit, Page](counterPath, _ => Page.Counter),
        SimpleRoute[Int, Page](
          moreComplexPath,
          (userId: Int) => Page.UserPage(userId)
        ),
        ComplexRoute[Option[Int], Page](
          pathWithParam,
          { case UrlMatching(_, ageOption) => Page.UserAgePage(ageOption) }
        )
      ),
      path
    )

}

case class AppState(page: Page, counter: Int)

type Model = AppState

enum Msg:
  case Increment, Decrement, Reset
  case Void
  case GoToCounterPage
  case GoToHomePage
