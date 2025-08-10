package tyrian.ui

import tyrian.next.GlobalMsg
import tyrian.next.Outcome
import tyrian.ui.utils.Lens

trait UIElement[Component, ComponentTheme]:

  def classNames: Set[String]
  def withClassNames(classes: Set[String]): Component
  def withClassNames(classes: String*): Component    = withClassNames(classes.toSet)
  def addClassNames(classes: Set[String]): Component = withClassNames(classNames ++ classes)
  def addClassNames(classes: String*): Component     = addClassNames(classes.toSet)

  def themeOverride: Option[ComponentTheme => ComponentTheme]
  def themeLens: Lens[Theme, ComponentTheme]
  def withThemeOverride(f: ComponentTheme => ComponentTheme): Component

  /** *Should not be called directly.* User provided implementation of a function to render the UIElement into a Tyrian
    * Elem[GlobalMsg] with the given theme, however, the correct way to render a UIElement is to call `toElem`, which
    * applies the theme overrides.
    */
  def view: Theme ?=> tyrian.Elem[GlobalMsg]

  /** Renders the current element to into a Tyrian Elem[GlobalMsg] with the given theme and theme overrides.
    */
  def toElem: Theme ?=> tyrian.Elem[GlobalMsg] =
    val overriddenTheme =
      applyThemeOverrides(summon[Theme])

    view(using overriddenTheme)

  /** An implementation detail, left open for testing purposes. Allows you to see how a given Theme will be modified by
    * the UIElement
    */
  def applyThemeOverrides(theme: Theme): Theme =
    themeOverride match
      case Some(g) =>
        themeLens.set(theme, g(themeLens.get(theme)))

      case None =>
        theme

object UIElement:

  trait Stateful[Component, ComponentTheme] extends UIElement[Component, ComponentTheme]:

    def key: UIKey
    def withKey(value: UIKey): Component

    def update: GlobalMsg => Outcome[Component]

/*

Theme

  - NoStyles

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
