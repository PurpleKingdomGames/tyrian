package example

import tyrian.Html.*
import tyrian.next.*

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
