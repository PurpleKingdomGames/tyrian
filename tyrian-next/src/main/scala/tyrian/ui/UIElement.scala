package tyrian.ui

import tyrian.next.GlobalMsg
import tyrian.next.Outcome

trait UIElement[T]:

  def classNames: Set[String]
  def withClassNames(classes: Set[String]): T
  def withClassNames(classes: String*): T    = withClassNames(classes.toSet)
  def addClassNames(classes: Set[String]): T = withClassNames(classNames ++ classes)
  def addClassNames(classes: String*): T     = addClassNames(classes.toSet)

  def overrideLocalTheme: Option[Theme => Theme]
  def withThemeOverride(f: Theme => Theme): T

  def view: Theme ?=> tyrian.Elem[GlobalMsg]

object UIElement:

  trait Stateful[T] extends UIElement[T]:

    def id: UIElementId
    def withId(value: UIElementId): T

    def update: GlobalMsg => Outcome[T]

opaque type UIElementId = String
object UIElementId:

  given CanEqual[UIElementId, UIElementId] = CanEqual.derived

  def apply(value: String): UIElementId = value

  extension (id: UIElementId) def value: String = id

/*

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
