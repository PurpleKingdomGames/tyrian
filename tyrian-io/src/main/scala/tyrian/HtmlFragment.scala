package tyrian

enum HtmlFragment:
  case MarkUp(entries: Batch[Html[GlobalMsg]])
  case Insert(markerId: MarkerId, entries: Batch[Html[GlobalMsg]])

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

  def addHtml(more: Batch[Html[GlobalMsg]]): HtmlFragment =
    this match
      case hf: MarkUp => hf.copy(entries = hf.entries ++ more)
      case hf: Insert => hf.copy(entries = hf.entries ++ more)
  def addHtml(more: Html[GlobalMsg]*): HtmlFragment =
    addHtml(Batch.fromSeq(more))

  def withHtml(newEntries: Batch[Html[GlobalMsg]]): HtmlFragment =
    this match
      case hf: MarkUp => hf.copy(entries = newEntries)
      case hf: Insert => hf.copy(entries = newEntries)
  def withHtml(newEntries: Html[GlobalMsg]*): HtmlFragment =
    withHtml(Batch.fromSeq(newEntries))

  def combine(other: HtmlFragment): HtmlFragment =
    HtmlFragment.combine(this, other)
  def |+|(other: HtmlFragment): HtmlFragment =
    HtmlFragment.combine(this, other)

  def toHtml: Batch[Html[GlobalMsg]] =
    this match
      case MarkUp(entries)    => entries
      case Insert(_, entries) => entries

object HtmlFragment:

  def combine(a: HtmlFragment, b: HtmlFragment): HtmlFragment =
    (a, b) match
      case (HtmlFragment.MarkUp(esA), HtmlFragment.MarkUp(esB)) =>
        HtmlFragment.MarkUp(esA ++ esB)

      case (HtmlFragment.MarkUp(esA), HtmlFragment.Insert(id, esB)) =>
        HtmlFragment.MarkUp(insert(esB, esA, id))

      case (HtmlFragment.Insert(id, esA), HtmlFragment.MarkUp(esB)) =>
        HtmlFragment.Insert(id, esA ++ esB)

      case (HtmlFragment.Insert(idA, esA), HtmlFragment.Insert(idB, esB)) =>
        HtmlFragment.Insert(idA, insert(esB, esA, idB))

  def insert(entries: Batch[Html[GlobalMsg]], into: Batch[Html[GlobalMsg]], at: MarkerId): Batch[Html[GlobalMsg]] =
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

    def recHtml(into: Html[GlobalMsg]): Html[GlobalMsg] =
      into match
        case t: Tag[GlobalMsg] =>
          t.copy(children = t.children.map(recElem))

        case r: RawTag[GlobalMsg] =>
          r

        case c: CustomHtml[GlobalMsg] =>
          c

    into.map(recHtml)

  def empty: HtmlFragment =
    HtmlFragment.MarkUp(Batch.empty)

  def apply(markup: Html[GlobalMsg]*): HtmlFragment =
    HtmlFragment.MarkUp(Batch.fromSeq(markup))

  object MarkUp:

    def apply(markup: Html[GlobalMsg]): HtmlFragment.MarkUp =
      HtmlFragment.MarkUp(Batch(markup))

  object Insert:

    def apply(id: MarkerId, markup: Html[GlobalMsg]): HtmlFragment.Insert =
      HtmlFragment.Insert(id, Batch(markup))
