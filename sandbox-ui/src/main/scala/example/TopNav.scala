package example

import tyrian.next.*
import tyrian.ui.*

final case class TopNav():

  def update: GlobalMsg => Outcome[TopNav] =
    case _ => Outcome(this)

  def view(using Theme): HtmlFragment =
    HtmlFragment(
      Row(
        Link("/another-page")(
          TextBlock("Internal link (will be ignored)")
        ),
        Link("http://tyrian.indigoengine.io/")(
          TextBlock("Tyrian website")
        ).withTarget(Target.Blank) // TODO: Target does nothing - something about the routing I guess.
      ).spaceAround.toHtml
    )

object TopNav:
  val initial: TopNav =
    TopNav()
