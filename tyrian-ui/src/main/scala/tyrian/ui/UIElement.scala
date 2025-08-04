package tyrian.ui

trait UIElement[T, +Msg]:

  def classNames: Set[String]
  def withClassNames(classes: Set[String]): T
  def withClassNames(classes: String*): T    = withClassNames(classes.toSet)
  def addClassNames(classes: Set[String]): T = withClassNames(classNames ++ classes)
  def addClassNames(classes: String*): T     = addClassNames(classes.toSet)

  def _modifyTheme: Option[Theme => Theme]
  def modifyTheme(f: Theme => Theme): T

  def toHtml: Theme ?=> tyrian.Html[Msg]

/*

Stateless Components
	-	Text
	-	Image
	-	Icon
	-	Row
	-	Column
	-	El
	-	Spacer
	-	Paragraph
	-	WrappedRow
	-	WrappedColumn
	-	Padding
	-	CenterX
	-	CenterY
	-	AlignRight
	-	Width
	-	Height
	-	Background
	-	Border
	-	Font
	-	Color
	-	None
	-	HtmlElement
	-	Divider
	-	Label

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
 */
