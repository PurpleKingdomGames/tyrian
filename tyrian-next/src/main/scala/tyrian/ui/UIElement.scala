package tyrian.ui

import tyrian.next.GlobalMsg

trait UIElement[T]:

  // TODO: Bring back `map` over GlobalMsg's function

  def classNames: Set[String]
  def withClassNames(classes: Set[String]): T
  def withClassNames(classes: String*): T    = withClassNames(classes.toSet)
  def addClassNames(classes: Set[String]): T = withClassNames(classNames ++ classes)
  def addClassNames(classes: String*): T     = addClassNames(classes.toSet)

  def _modifyTheme: Option[Theme => Theme]
  def modifyTheme(f: Theme => Theme): T

  def toHtml: Theme ?=> tyrian.Elem[GlobalMsg]

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
