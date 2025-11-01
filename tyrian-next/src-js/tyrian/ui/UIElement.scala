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

  def id: Option[String]
  def withId(id: String): Component

  def themeOverride: ThemeOverride[ComponentTheme]
  def themeLens: Lens[Theme.Default, ComponentTheme]
  def withThemeOverride(value: ThemeOverride[ComponentTheme]): Component
  def noTheme: Component =
    withThemeOverride(ThemeOverride.NoTheme)
  def useDefaultTheme: Component =
    withThemeOverride(ThemeOverride.NoOverride)
  def overrideTheme(modify: ComponentTheme => ComponentTheme): Component =
    withThemeOverride(ThemeOverride.Override(modify))

  /** Warning: Should not be called directly. User provided implementation of a function to render the UIElement into a
    * Tyrian Elem[GlobalMsg] with the given theme, however, the correct way to render a UIElement is to call `toElem`,
    * which applies any theme overrides.
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

---

Cross compilation - we're going to want this for Scala JVM use.

Look at Theme's carefully. Link should use the same theme as Text, for example.

New library of shared types (Tyrian / Indigo):

  - Batch
  - Lens
  - Signal
  - SignalFunction
  - Timeline Animations?
  - RGBA?
  - RGB?
  - (Look at the other data type classes)

---

Theme / Style Performance

  1. Font styles inherit, and they're very chunky. So what we could do is track
  what's been set so far down any given rendering branch, and only set the style
  tag for the changes. Might mean setting font details on Containers?

  2. The themes are a bit mixed at the moment. Review which fields should and shouldn't be optional to avoid needlessly writing out default styles. Also some styles might be set, but if they're the default, don't render.

  3. Hash styles, render them in a style block, and use a class name based on the hash. This avoids huge styles, and makes point (1) redundant.

---

Stateless Components

	-	Button
	-	Text - DONE
	-	Image -DONE
	-	Row - DONE
	-	Column - DONE
	-	El / Container - DONE
	-	Paragraph - DONE
	-	HtmlElement - DONE
  - Link - DONE
  - Tables - DONE(ish)
  - Canvas (2D, WebGL 1/2, WebGPU) - DONE (basic)

⸻

Stateful Components

	-	Input - DONE
	-	TextArea
	-	Checkbox /  Switch (type of checkbox...?)
	-	Radio buttons
	-	Slider / Range
	-	Tooltip
  - Dropdown / chooser

Compound Components?

  - Carousel
  - inplace input
  - Notifications
  - Datepicker
  - Color picker
	-	FileInput / FileManager

---

Transitions

I think generally these should just either be tastefully built in, no choice, or applied by the user adding custom styles.

---

Drawing API

  - Compiles to SVG?

---

Animations?

---

Future enhancements:

  - Buttons are stateless, make stateful for mouse over / down effects.

 */
