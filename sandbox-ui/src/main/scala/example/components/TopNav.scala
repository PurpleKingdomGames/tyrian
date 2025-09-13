package example.components

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
        ).withTarget(Target.Blank)
      ).spaceAround
    )

object TopNav:
  val initial: TopNav =
    TopNav()
