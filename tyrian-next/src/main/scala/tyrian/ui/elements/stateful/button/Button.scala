package tyrian.ui.elements.stateful.button

import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui.UIElement
import tyrian.ui.UIKey
import tyrian.ui.theme.Theme
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

final case class Button(
    key: UIKey,
    classNames: Set[String],
    themeOverride: ThemeOverride[ButtonTheme]
) extends UIElement.Stateful[Button, ButtonTheme]:
  
  def update: GlobalMsg => Outcome[Button] =
    ???

  def withKey(value: UIKey): Button =
    ???
  
  def themeLens: Lens[Theme.Default, ButtonTheme] =
    ???

  def view: Theme ?=> tyrian.Elem[GlobalMsg] =
    ???

  def withClassNames(classes: Set[String]): Button =
    this.copy(classNames = classes)

  def withThemeOverride(value: ThemeOverride[ButtonTheme]): Button =
    this.copy(themeOverride = value)
