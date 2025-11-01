package tyrian.ui.layout

import tyrian.next.GlobalMsg
import tyrian.next.Marker
import tyrian.next.MarkerId
import tyrian.ui.UIElement
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Placeholder(
    marker: MarkerId,
    children: List[UIElement[?, ?]]
) extends UIElement[Placeholder, Unit]:

  val classNames: Set[String]            = Set()
  val id: Option[String]                 = None
  val themeOverride: ThemeOverride[Unit] = ThemeOverride.NoOverride

  def withChildren(children: UIElement[?, ?]*): Placeholder =
    this.copy(children = children.toList)

  def addChild(child: UIElement[?, ?]): Placeholder =
    this.copy(children = children :+ child)

  def withClassNames(classes: Set[String]): Placeholder =
    this

  def withId(id: String): Placeholder =
    this

  def themeLens: Lens[Theme.Default, Unit] =
    Lens.unit

  def withThemeOverride(value: ThemeOverride[Unit]): Placeholder =
    this

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    Marker(marker, children.map(_.toElem))

object Placeholder:

  def apply(marker: MarkerId): Placeholder =
    Placeholder(marker, Nil)

  def apply(marker: MarkerId, children: UIElement[?, ?]*): Placeholder =
    Placeholder(marker, children.toList)
