package tyrian.ui.layout

import tyrian.next.GlobalMsg
import tyrian.next.Marker
import tyrian.next.MarkerId
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Placeholder(
    id: MarkerId,
    children: List[UIElement[?, ?]],
    classNames: Set[String],
    themeOverride: ThemeOverride[Unit]
) extends UIElement[Placeholder, Unit]:

  def withChildren(children: UIElement[?, ?]*): Placeholder =
    this.copy(children = children.toList)

  def addChild(child: UIElement[?, ?]): Placeholder =
    this.copy(children = children :+ child)

  def withClassNames(classes: Set[String]): Placeholder =
    this.copy(classNames = classes)

  def themeLens: Lens[Theme.Default, Unit] =
    Lens.unit

  def withThemeOverride(value: ThemeOverride[Unit]): Placeholder =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Marker(id, children.map(_.toElem))

object Placeholder:

  def apply(id: MarkerId): Placeholder =
    Placeholder(id, Nil, Set(), ThemeOverride.NoOverride)

  def apply(id: MarkerId, children: UIElement[?, ?]*): Placeholder =
    Placeholder(id, children.toList, Set(), ThemeOverride.NoOverride)

  def apply(id: MarkerId, children: List[UIElement[?, ?]]): Placeholder =
    Placeholder(id, children.toList, Set(), ThemeOverride.NoOverride)
