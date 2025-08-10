package tyrian.ui.layout

import tyrian.next.GlobalMsg
import tyrian.next.Marker
import tyrian.next.MarkerId
import tyrian.ui.Theme
import tyrian.ui.UIElement

final case class Placeholder(
    id: MarkerId,
    children: List[UIElement[?]],
    classNames: Set[String],
    overrideLocalTheme: Option[Theme => Theme]
) extends UIElement[Placeholder]:

  def withChildren(children: UIElement[?]*): Placeholder =
    this.copy(children = children.toList)

  def addChild(child: UIElement[?]): Placeholder =
    this.copy(children = children :+ child)

  def withClassNames(classes: Set[String]): Placeholder =
    this.copy(classNames = classes)

  def withThemeOverride(f: Theme => Theme): Placeholder =
    this.copy(overrideLocalTheme = Some(f))

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Placeholder.toHtml(this)

object Placeholder:

  def apply(id: MarkerId): Placeholder =
    Placeholder(id, Nil, Set(), None)

  def apply(id: MarkerId, children: UIElement[?]*): Placeholder =
    Placeholder(id, children.toList, Set(), None)

  def apply(id: MarkerId, children: List[UIElement[?]]): Placeholder =
    Placeholder(id, children.toList, Set(), None)

  def toHtml(element: Placeholder)(using theme: Theme): tyrian.Elem[GlobalMsg] =
    val t =
      element.overrideLocalTheme match
        case Some(f) => f(theme)
        case None    => theme

    val htmlChildren = element.children.map(_.view(using t))

    Marker(element.id, htmlChildren)
