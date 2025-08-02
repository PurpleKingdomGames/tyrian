package tyrian.next

import tyrian.CustomElem
import tyrian.CustomHtml
import tyrian.Elem
import tyrian.Empty
import tyrian.Html
import tyrian.RawTag
import tyrian.Tag
import tyrian.Text

enum HtmlFragment:
  case MarkUp(entries: Batch[Elem[GlobalMsg]])
  case Insert(markerId: MarkerId, entries: Batch[Elem[GlobalMsg]])

  def map(f: GlobalMsg => GlobalMsg): HtmlFragment =
    this match
      case MarkUp(entries) =>
        MarkUp(entries.map(_.map(f)))

      case hf @ Insert(_, entries) =>
        hf.copy(entries = entries.map(_.map(f)))

  def withMarkerId(mid: MarkerId): HtmlFragment =
    this match
      case MarkUp(entries) => HtmlFragment.Insert(mid, entries)
      case hf: Insert      => hf.copy(markerId = mid)

  def clearMarkerId: HtmlFragment =
    this match
      case hf: MarkUp         => hf
      case Insert(_, entries) => HtmlFragment.MarkUp(entries)

  def addHtml(more: Batch[Elem[GlobalMsg]]): HtmlFragment =
    this match
      case hf: MarkUp => hf.copy(entries = hf.entries ++ more)
      case hf: Insert => hf.copy(entries = hf.entries ++ more)
  def addHtml(more: Elem[GlobalMsg]*): HtmlFragment =
    addHtml(Batch.fromSeq(more))

  def withHtml(newEntries: Batch[Elem[GlobalMsg]]): HtmlFragment =
    this match
      case hf: MarkUp => hf.copy(entries = newEntries)
      case hf: Insert => hf.copy(entries = newEntries)
  def withHtml(newEntries: Elem[GlobalMsg]*): HtmlFragment =
    withHtml(Batch.fromSeq(newEntries))

  def combine(other: HtmlFragment): HtmlFragment =
    HtmlFragment.combine(this, other)
  def |+|(other: HtmlFragment): HtmlFragment =
    HtmlFragment.combine(this, other)

  def toHtmlRoot(surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg]): HtmlRoot =
    HtmlRoot(surround, this)
  def toHtmlRoot: HtmlRoot =
    HtmlRoot(this)

object HtmlFragment:

  def combine(a: HtmlFragment, b: HtmlFragment): HtmlFragment =
    (a, b) match
      case (HtmlFragment.MarkUp(esA), HtmlFragment.MarkUp(esB)) =>
        HtmlFragment.MarkUp(esA ++ esB)

      case (HtmlFragment.MarkUp(esA), HtmlFragment.Insert(id, esB)) =>
        HtmlFragment.MarkUp(insert(esB, esA, id))

      case (HtmlFragment.Insert(id, esA), HtmlFragment.MarkUp(esB)) =>
        HtmlFragment.Insert(id, esA ++ esB)

      case (HtmlFragment.Insert(idA, esA), HtmlFragment.Insert(idB, esB)) if idA == idB =>
        HtmlFragment.Insert(idA, esA ++ esB)

      case (HtmlFragment.Insert(idA, esA), HtmlFragment.Insert(idB, esB)) =>
        HtmlFragment.Insert(idA, insert(esB, esA, idB))

  def insert(entries: Batch[Elem[GlobalMsg]], into: Batch[Elem[GlobalMsg]], at: MarkerId): Batch[Elem[GlobalMsg]] =
    def recElem(elem: Elem[GlobalMsg]): Elem[GlobalMsg] =
      elem match
        case Empty =>
          Empty

        case t: Text =>
          t

        case m: Marker if m.id == at =>
          m.copy(children = m.children ++ entries.toList)

        case m: Marker =>
          m.copy(children = m.children.map(recElem))

        case t: Tag[GlobalMsg] =>
          t.copy(children = t.children.map(recElem))

        case r: RawTag[GlobalMsg] =>
          r

        case c: CustomElem[GlobalMsg] =>
          c

        case c: CustomHtml[GlobalMsg] =>
          c

    into.map(recElem)

  def empty: HtmlFragment =
    HtmlFragment.MarkUp(Batch.empty)

  def apply(markup: Elem[GlobalMsg]*): HtmlFragment =
    HtmlFragment.MarkUp(Batch.fromSeq(markup))

  object MarkUp:

    def apply(markup: Elem[GlobalMsg]*): HtmlFragment.MarkUp =
      HtmlFragment.MarkUp(Batch.fromSeq(markup))

  object Insert:

    def apply(id: MarkerId, markup: Elem[GlobalMsg]*): HtmlFragment.Insert =
      HtmlFragment.Insert(id, Batch.fromSeq(markup))
