package myorg

import cats.effect.IO
import org.scalajs.dom
import tyrian.Html.{param => _, _}
import tyrian.*
import urldsl.language.simpleErrorImpl.*
import urldsl.vocabulary.Param
import urldsl.vocabulary.Segment
import urldsl.vocabulary.UrlMatching

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("TyrianApp")
object HelloTyrian extends TyrianApp[Msg, Model]:

  val homePath        = root / "home"
  val counterPath     = root / "counter"
  val moreComplexPath = root / "id" / segment[Int]
  val pathWithParam   = (root / "user" / endOfSegments) ? param[Int]("age").?

  private def getPageFromURL(path: String): Page =
    homePath
      .matchRawUrl(path)
      .fold(
        _ =>
          counterPath
            .matchRawUrl(path)
            .fold(
              _ =>
                moreComplexPath
                  .matchRawUrl(path)
                  .fold(
                    _ =>
                      pathWithParam
                        .matchRawUrl(path)
                        .fold(
                          _ => Page.NotFound,
                          { case UrlMatching(_, ageOption) =>
                            Page.UserAgePage(ageOption)
                          }
                        ),
                    userId => Page.UserPage(userId)
                  ),
              _ => Page.Counter
            ),
        _ => Page.Home
      )

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (
      AppState(getPageFromURL(dom.window.location.href), counter = 0),
      Cmd.None
    )

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Increment => (model.copy(counter = model.counter + 1), Cmd.None)
    case Msg.Decrement => (model.copy(counter = model.counter - 1), Cmd.None)
    case Msg.Reset     => (model.copy(counter = 0), Cmd.None)

  def viewHome(model: Model): Html[Msg] =
    div(p("Hello from home!"))

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
        )("Reset")
    )

  def viewNotFound(): Html[Msg] =
    div(p("Not found the requested page"))

  def viewUserPage(userId: Int): Html[Msg] =
    div(p(s"Welcome at user page with user id: ${userId}"))

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

case class AppState(page: Page, counter: Int)

type Model = AppState

enum Msg:
  case Increment, Decrement, Reset
