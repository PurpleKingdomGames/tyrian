package tyrian.next

import tyrian.CustomElem
import tyrian.Elem

import scala.annotation.nowarn

/** A marker in the Html for Tyrian to latch onto, that is never seen by the VirtualDom. */
final case class Marker(id: MarkerId, children: List[Elem[GlobalMsg]]) extends CustomElem[GlobalMsg]:

  @nowarn
  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def map[N](f: GlobalMsg => N): CustomElem[N] =
    /* There is some nasty stuff going on here. Essentially, we need to convince the compiler that `N`
       is a `GlobalMsg`, even when it might not be. In practice, this is only used in the new stuff where
       Msg == GlobalMsg always, which means that all the Html[?] and Elem[?] instances have be
       Html/Elem[GlobalMsg], and so even though we're allowed here to some weird stuff, actually, the
       compiler saves us higher up by refusing to accept a map function that doesn't result in a
       CustomElem[GlobalMsg].
     */
    children.map(_.map(f)) match
      case cs: List[Elem[GlobalMsg]] =>
        this
          .copy(children = cs)
          .asInstanceOf[CustomElem[N]]

      case _ =>
        throw new IllegalArgumentException("Marker.map: Only GlobalMsg => GlobalMsg supported")

  /** Extract the child elements from the marker. */
  def toElems: List[Elem[GlobalMsg]] =
    children

object Marker:
  /** Creates a marker without child elements. */
  def apply(id: MarkerId): Marker =
    Marker(id, Nil)

  /** Creates a marker with child elements. */
  def apply(id: MarkerId, children: Elem[GlobalMsg]*): Marker =
    Marker(id, children.toList)

  /** Creates a marker with child elements. */
  def apply(id: MarkerId, children: List[Elem[GlobalMsg]]): Marker =
    Marker(id, children.toList)
