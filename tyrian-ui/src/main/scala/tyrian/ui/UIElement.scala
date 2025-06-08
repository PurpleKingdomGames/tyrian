package tyrian.ui

trait UIElement[+Msg]:
  type T <: UIElement[Msg]

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
