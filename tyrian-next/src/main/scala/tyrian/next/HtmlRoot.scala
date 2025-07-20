package tyrian.next

import tyrian.Html

final case class HtmlRoot(surround: Batch[Html[GlobalMsg]] => Html[GlobalMsg], fragment: HtmlFragment):

  def withHtmlFragment(value: HtmlFragment): HtmlRoot =
    this.copy(fragment = value)

  def combine(next: HtmlFragment): HtmlRoot =
    this.copy(fragment = fragment.combine(next))
  def |+|(frag: HtmlFragment): HtmlRoot =
    combine(frag)

  def toHtml: Html[GlobalMsg] =
    surround(fragment.toHtml)

object HtmlRoot:

  def apply(surround: Batch[Html[GlobalMsg]] => Html[GlobalMsg]): HtmlRoot =
    HtmlRoot(surround, HtmlFragment.empty)

  def div(fragment: HtmlFragment): HtmlRoot =
    HtmlRoot(
      childNodes => tyrian.Html.div(childNodes.toList),
      fragment
    )

  def span(fragment: HtmlFragment): HtmlRoot =
    HtmlRoot(
      childNodes => tyrian.Html.span(childNodes.toList),
      fragment
    )
