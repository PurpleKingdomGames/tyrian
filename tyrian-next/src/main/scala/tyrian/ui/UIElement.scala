package tyrian.ui

import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui.theme.ThemeOverride
import tyrian.ui.utils.Lens

trait UIElement[Component, ComponentTheme]:

  def classNames: Set[String]
  def withClassNames(classes: Set[String]): Component
  def withClassNames(classes: String*): Component    = withClassNames(classes.toSet)
  def addClassNames(classes: Set[String]): Component = withClassNames(classNames ++ classes)
  def addClassNames(classes: String*): Component     = addClassNames(classes.toSet)

  def themeOverride: ThemeOverride[ComponentTheme]
  def themeLens: Lens[Theme.Default, ComponentTheme]
  def withThemeOverride(value: ThemeOverride[ComponentTheme]): Component
  def noTheme: Component =
    withThemeOverride(ThemeOverride.NoTheme)
  def useDefaultTheme: Component =
    withThemeOverride(ThemeOverride.NoOverride)
  def overrideTheme(modify: ComponentTheme => ComponentTheme): Component =
    withThemeOverride(ThemeOverride.Override(modify))

  /** *Should not be called directly.* User provided implementation of a function to render the UIElement into a Tyrian
    * Elem[GlobalMsg] with the given theme, however, the correct way to render a UIElement is to call `toElem`, which
    * applies any theme overrides.
    */
  def view: Theme ?=> tyrian.Elem[GlobalMsg]

  /** Renders the current element to into a Tyrian Elem[GlobalMsg] with the given theme and theme overrides.
    */
  def toElem: Theme ?=> tyrian.Elem[GlobalMsg] =
    val overriddenTheme =
      summon[Theme] match
        case t @ Theme.None =>
          t

        case t: Theme.Default =>
          applyThemeOverrides(t)

    view(using overriddenTheme)

  def applyThemeOverrides(theme: Theme.Default): Theme =
    themeOverride match
      case ThemeOverride.RemoveTheme() =>
        Theme.None

      case ThemeOverride.DoNotOverride() =>
        theme

      case ThemeOverride.Override[ComponentTheme](modify) =>
        themeLens.set(theme, modify(themeLens.get(theme)))

object UIElement:

  trait Stateful[Component, ComponentTheme] extends UIElement[Component, ComponentTheme]:

    def key: UIKey
    def withKey(value: UIKey): Component

    def update: GlobalMsg => Outcome[Component]

/*

TODOs

Theme

  - NoStyles

Theme / Style Performance

  - Font styles inherit, and they're very chunky. So what we could do is track
  what's been set so far down any given rendering branch, and only set the style
  tag for the changes. Might mean setting font details on Containers?

  - The themes are a bit mixed at the moment. Review which fields should and shouldn't be optional to avoid needlessly writing out default styles. Also some styles might be set, but if they're the default, don't render.
---

Stateless Components
	-	Text - DONE
	-	Image -DONE
	-	Icon
	-	Row - DONE
	-	Column - DONE
	-	El / Container - DONE
	-	Spacer
	-	Paragraph - DONE
	-	WrappedRow
	-	WrappedColumn
	-	HtmlElement - DONE
	-	Divider
	-	Label
  - Link
  - Tables


⸻

Stateful Components
	-	Button
	-	Input
	-	TextArea
	-	Checkbox
	-	Radio
	-	Slider
	-	Switch
	-	Focusable
	-	Hoverable
	-	Tooltip
	-	ProgressBar
	-	FileInput
  - Notifications
  - Datepicker
  - Dropdown / chooser
  - Carousel
  - inplace input
 */
