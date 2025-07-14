package example

import tyrian.*
import tyrian.Html.*

final case class TopNav():

  def update: GlobalMsg => Outcome[TopNav] =
    case _ => Outcome(this)

  def view: HtmlFragment =
    HtmlFragment(
      div(
        a(href := "/another-page")("Internal link (will be ignored)"),
        br,
        a(href := "http://tyrian.indigoengine.io/")("Tyrian website")
      )
    )

object TopNav:
  val initial: TopNav =
    TopNav()
