package tyrian.next

import tyrian.CustomElem
import tyrian.CustomHtml
import tyrian.Elem
import tyrian.Empty
import tyrian.Html
import tyrian.RawTag
import tyrian.Tag
import tyrian.Text

/** The `HtmlRoot` performs two functions. First, it satisfies the VirtualDom's requirement for there to be one top
  * level HTML tag. Second, it resolves the contents of an HtmlFragment into a DOM tree, and turns it into usable HTML
  * that the VirtualDom can render.
  */
final case class HtmlRoot(
    surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg],
    fragment: HtmlFragment
):

  /** Provide a way to convert a batch of elems into a single elem, which means wrapping the elems in an HTML tag. */
  def withSurround(elemsToHtml: Batch[Elem[GlobalMsg]] => Html[GlobalMsg]): HtmlRoot =
    this.copy(surround = elemsToHtml)

  /** Provide the HtmlFragment to render */
  def withHtmlFragment(value: HtmlFragment): HtmlRoot =
    this.copy(fragment = value)

  /** Append a batch of HtmlFragments to the existing fragment */
  def addHtmlFragments(fragments: Batch[HtmlFragment]): HtmlRoot =
    withHtmlFragment(
      fragments.foldLeft(fragment)(_ |+| _)
    )

  /** Append a sequence of HtmlFragments to the existing fragment */
  def addHtmlFragments(fragments: HtmlFragment*): HtmlRoot =
    addHtmlFragments(Batch.fromSeq(fragments))

  /** Process the HtmlFragment into HTML that can be rendered by the VirtualDom */
  def toHtml: Html[GlobalMsg] =
    HtmlRoot.resolve(this)

object HtmlRoot:

  /** Creates an HtmlRoot with a custom surround function and an empty fragment. */
  def apply(surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg]): HtmlRoot =
    HtmlRoot(surround, HtmlFragment.empty)

  /** Creates an HtmlRoot that wraps a fragment's markup in a div element. */
  def apply(fragment: HtmlFragment): HtmlRoot =
    HtmlRoot(
      childNodes => Html.div(childNodes.toList),
      fragment
    )

  /** Creates an HtmlRoot by combining a Batch of HtmlFragments and wrapping them in a div */
  def apply(fragments: Batch[HtmlFragment]): HtmlRoot =
    HtmlRoot(fragments.foldLeft(HtmlFragment.empty)(_ |+| _))

  /** Creates an HtmlRoot by combining a repeating number of HtmlFragments and wrapping them in a div */
  def apply(fragments: HtmlFragment*): HtmlRoot =
    HtmlRoot(Batch.fromSeq(fragments))

  /** Resolves an HtmlRoot into final HTML by replacing all markers with their corresponding inserts. If the Marker
    * contains children, they are prepended to the output.
    */
  def resolve(root: HtmlRoot): Html[GlobalMsg] =
    def toHtml: Batch[Elem[GlobalMsg]] =
      root.fragment.markup.flatMap(m => resolveMarkersForElem(m, root.fragment.inserts))

    root.surround(toHtml)

  private def resolveMarkersForElem(
      target: Elem[GlobalMsg],
      inserts: Map[MarkerId, Batch[Elem[GlobalMsg]]]
  ): Batch[Elem[GlobalMsg]] =
    def rec(target: Elem[GlobalMsg]): List[Elem[GlobalMsg]] =
      target match
        case m: Marker =>
          inserts.get(m.id) match
            case None =>
              m.children.flatMap(rec(_).toList)

            case Some(toAppend) =>
              (m.children ++ toAppend.toList).flatMap(rec(_).toList)

        case t: Tag[GlobalMsg] =>
          List(t.copy(children = t.children.flatMap(rec(_))))

        case Empty =>
          List(Empty)

        case t: Text =>
          List(t)

        case c: CustomElem[GlobalMsg] =>
          List(c)

        case r: RawTag[GlobalMsg] =>
          List(r)

        case c: CustomHtml[GlobalMsg] =>
          List(c)

    Batch.fromList(rec(target))
