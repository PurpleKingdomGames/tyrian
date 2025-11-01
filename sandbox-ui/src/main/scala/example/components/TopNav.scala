package example.components

import example.AppEvent
import tyrian.next.*
import tyrian.ui.*

final case class TopNav():

  def update: GlobalMsg => Outcome[TopNav] =
    case _ => Outcome(this)

  def view(using Theme): HtmlFragment =
    HtmlFragment.insert(
      MarkerId("top-nav"),
      Row(
        Link("/another-page")(
          TextBlock("Internal link (will be ignored)")
        ),
        Link("http://tyrian.indigoengine.io/")(
          TextBlock("Tyrian website")
        ).withTarget(Target.Blank),
        Button("Shadertoy", AppEvent.FollowLink("https://shadertoy.com/"))
      ).spaceAround
    )

object TopNav:
  val initial: TopNav =
    TopNav()
