package tyrian.next

import tyrian.Elem
import tyrian.Html
import tyrian.ui.Theme
import tyrian.ui.UIElement

/** An HtmlFragment represents a chunk of Html that will potentially make up part of the final DOM output. Though it
  * aids 'out-of-order' dom tree construction, it is not the same as a template (which in Tyrian Next is any function of
  * some data to Elem[GlobalMsg]). Instead it is either a) markup to be appended, and / or b) markup to inserted.
  *
  * The normal form of an HtmlFragment is as a number of chunks of markup, that will ultimately be appended to some
  * parent div, typically a div. This is the top level structure of your page.
  *
  * HtmlFragments can be combined using the `combine` method (or `|+|` operator, which is an alias for combine).
  *
  * If you'd like, in some separate process, to be able to render something that goes into that top level structure,
  * than you can add a placeholder tag to your top level markup called a `Marker`. When you render the html to be
  * associated with the marker, you add it to an HtmlFragment fragment using the `insert` method (or constructor) and
  * give it the same ID as your placeholder Marker tag.
  *
  * Markers have an ID, and can also include child elements. When your HtmlFragments are resolved by the `HtmlRoot` it
  * attempts to replace markers with an insert that has the same id.
  */
final case class HtmlFragment(markup: Batch[Elem[GlobalMsg]], inserts: Map[MarkerId, Batch[Elem[GlobalMsg]]]):

  /** Add elements to the top level Markup */
  def append(additional: Batch[Elem[GlobalMsg]]): HtmlFragment =
    this.copy(markup = markup ++ additional)

  /** Add elements to the top level Markup */
  def append(additional: Elem[GlobalMsg]*): HtmlFragment =
    append(Batch.fromSeq(additional))

  /** Replace the current top level Markup elements with new ones */
  def replace(newMarkup: Batch[Elem[GlobalMsg]]): HtmlFragment =
    this.copy(markup = newMarkup)

  /** Replace the current top level Markup elements with new ones */
  def replace(newMarkup: Elem[GlobalMsg]*): HtmlFragment =
    replace(Batch.fromSeq(newMarkup))

  /** Adds elements to the insert map, which is used to resolve markers before rendering */
  def insert(at: MarkerId, elements: Batch[Elem[GlobalMsg]]): HtmlFragment =
    this.copy(inserts = inserts ++ Map(at -> elements))

  /** Adds elements to the insert map, which is used to resolve markers before rendering */
  def insert(at: MarkerId, elements: Elem[GlobalMsg]*): HtmlFragment =
    insert(at, Batch.fromSeq(elements))

  /** Removes 'insert' elements from the marker look up data by ID */
  def remove(markerId: MarkerId): HtmlFragment =
    this.copy(inserts = inserts.removed(markerId))

  /** Filters 'insert' elements in the marker look up data by ID */
  def filter(p: MarkerId => Boolean): HtmlFragment =
    this.copy(inserts = inserts.view.filterKeys(p).toMap)

  /** Performs a negative filter on 'insert' elements in the marker look up data by ID */
  def filterNot(p: MarkerId => Boolean): HtmlFragment =
    this.copy(inserts = inserts.view.filterKeys(k => !p(k)).toMap)

  /** Combines two HtmlFragments by appending / merging their markup elements and insert data respectively. */
  def combine(other: HtmlFragment): HtmlFragment =
    HtmlFragment.combine(this, other)

  /** Combines two HtmlFragments by appending / merging their markup elements and insert data respectively. */
  def |+|(other: HtmlFragment): HtmlFragment =
    HtmlFragment.combine(this, other)

  /** Convery this HtmlFragment into an HtmlRoot by supplying a surround function */
  def toHtmlRoot(surround: Batch[Elem[GlobalMsg]] => Html[GlobalMsg]): HtmlRoot =
    HtmlRoot(surround, this)

  /** Convery this HtmlFragment into an HtmlRoot by surrounding the elements with a `div` tag */
  def toHtmlRoot: HtmlRoot =
    HtmlRoot(this)

object HtmlFragment:

  /** Merges two HtmlFragments by concatenating their markup sequences and combining their insert maps. When both
    * fragments contain inserts for the same MarkerId, the second fragment's insert will override the first.
    */
  def combine(a: HtmlFragment, b: HtmlFragment): HtmlFragment =
    HtmlFragment(
      a.markup ++ b.markup,
      a.inserts ++ b.inserts
    )

  private val emptyInserts: Map[MarkerId, Batch[Elem[GlobalMsg]]] =
    Map.empty[MarkerId, Batch[Elem[GlobalMsg]]]

  /** Creates an empty HtmlFragment with no markup elements and no marker inserts. Useful as a starting point for
    * building fragments through combination or as a neutral element in fragment operations.
    */
  def empty: HtmlFragment =
    HtmlFragment(Batch.empty, emptyInserts)

  /** Creates an HtmlFragment containing only top-level markup elements, with no marker inserts. The elements will be
    * rendered directly as part of the fragment's markup when resolved by an HtmlRoot.
    */
  def apply(markup: Batch[Elem[GlobalMsg]]): HtmlFragment =
    HtmlFragment(markup, emptyInserts)

  /** Creates an HtmlFragment from a variable number of elements, converting them into a Batch internally. This is the
    * most common way to create fragments when you have individual elements to include.
    */
  def apply(markup: Elem[GlobalMsg]*): HtmlFragment =
    HtmlFragment(Batch.fromSeq(markup), emptyInserts)

  /** Creates an HtmlFragment containing only top-level ui elements, with no marker inserts. The elements will be
    * rendered directly as part of the fragment's markup when resolved by an HtmlRoot.
    */
  def apply(markup: Batch[UIElement[?]])(using Theme): HtmlFragment =
    HtmlFragment(markup.map(_.view), emptyInserts)

  /** Creates an HtmlFragment from a variable number of ui elements, converting them into a Batch internally. This is
    * the most common way to create fragments when you have individual elements to include.
    */
  def apply(markup: UIElement[?]*)(using Theme): HtmlFragment =
    HtmlFragment(Batch.fromSeq(markup).map(_.view), emptyInserts)

  /** Creates an HtmlFragment that contains only insert data for a specific marker, with no top-level markup. When
    * resolved, these elements will replace any Marker with the matching MarkerId in the final DOM tree.
    */
  def insert(at: MarkerId, elements: Batch[Elem[GlobalMsg]]): HtmlFragment =
    HtmlFragment(Batch.empty, Map(at -> elements))

  /** Creates an HtmlFragment that contains only insert data for a specific marker, with no top-level markup. When
    * resolved, these elements will replace any Marker with the matching MarkerId in the final DOM tree.
    */
  def insert(at: MarkerId, elements: Elem[GlobalMsg]*): HtmlFragment =
    insert(at, Batch.fromSeq(elements))

  /** Creates an HtmlFragment that contains only insert data for a specific marker / placeholder, with no top-level
    * markup. When resolved, these elements will replace any Marker (or Placeholder if using Tyrian-UI) with the
    * matching MarkerId in the final DOM tree.
    */
  def insert(at: MarkerId, elements: Batch[UIElement[?]])(using Theme): HtmlFragment =
    HtmlFragment(Batch.empty, Map(at -> elements.map(_.view)))

  /** Creates an HtmlFragment that contains only insert data for a specific marker / placeholder, with no top-level
    * markup. When resolved, these elements will replace any Marker (or Placeholder if using Tyrian-UI) with the
    * matching MarkerId in the final DOM tree.
    */
  def insert(at: MarkerId, elements: UIElement[?]*)(using Theme): HtmlFragment =
    insert(at, Batch.fromSeq(elements).map(_.view))
