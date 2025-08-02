package tyrian.next

import tyrian.Elem
import tyrian.Html

final case class HtmlRoot(
    surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg],
    fragment: HtmlFragment
):

  def withHtmlFragment(value: HtmlFragment): HtmlRoot =
    this.copy(fragment = value)

  def toHtml: Html[GlobalMsg] =
    HtmlRoot.resolve(this)

object HtmlRoot:

  def apply(surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg]): HtmlRoot =
    HtmlRoot(surround, HtmlFragment.empty)

  def apply(fragment: HtmlFragment): HtmlRoot =
    HtmlRoot(
      childNodes => Html.div(childNodes.toList),
      fragment
    )

  def resolve(root: HtmlRoot): Html[GlobalMsg] =
    def toHtml: Batch[Elem[GlobalMsg]] =
      root.fragment match
        case HtmlFragment.MarkUp(entries)    => entries
        case HtmlFragment.Insert(_, entries) => entries

    root.surround(toHtml)
