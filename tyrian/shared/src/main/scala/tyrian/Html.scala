package tyrian

import scala.annotation.targetName

/** An HTML element can be a tag or a text node */
sealed trait Elem[+M]:
  def map[N](f: M => N): Elem[N]

/** An Empty Node - renders nothing */
case object Empty extends Elem[Nothing]:
  def map[N](f: Nothing => N): Empty.type = this

/** A text node */
final case class Text(value: String) extends Elem[Nothing]:
  def map[N](f: Nothing => N): Text = this

/** Base class for HTML tags */
sealed trait Html[+M] extends Elem[M]:
  def map[N](f: M => N): Html[N]
  def innerHtml(html: String): Html[M]

/** Object used to provide Html syntax `import tyrian.Html.*`
  */
object Html extends HtmlTags with HtmlAttributes:

  def tag[M](name: String)(attributes: Attr[M]*)(children: Elem[M]*): Html[M] =
    Tag(name, attributes.toList, children.toList)
  @targetName("tag-list-repeated")
  def tag[M](name: String)(attributes: List[Attr[M]])(children: Elem[M]*): Html[M] =
    Tag(name, attributes, children.toList)
  @targetName("tag-repeated-list")
  def tag[M](name: String)(attributes: Attr[M]*)(children: List[Elem[M]]): Html[M] =
    Tag(name, attributes.toList, children)
  @targetName("tag-list-list")
  def tag[M](name: String)(attributes: List[Attr[M]])(children: List[Elem[M]]): Html[M] =
    Tag(name, attributes, children)

  def raw[M](name: String)(attributes: Attr[M]*)(html: String): Html[M] =
    RawTag(name, attributes.toList, html)
  @targetName("raw-list")
  def raw[M](name: String)(attributes: List[Attr[M]])(html: String): Html[M] =
    RawTag(name, attributes, html)

  // Custom tag syntax

  def radio[M](name: String, checked: Boolean, attributes: Attr[M]*): Html[M] =
    radio(name, checked, attributes.toList)
  @targetName("radio-list")
  def radio[M](name: String, checked: Boolean, attributes: List[Attr[M]]): Html[M] =
    input(
      List(
        Property("type", "radio"),
        Property("name", name),
        if checked then Property("checked", "checked") else Property.empty
      ) ++ attributes: _*
    )

  def text(plainText: String): Text = Text(plainText)

  // Custom attribute syntax

  def dataAttr(name: String, value: String): Attr[Nothing] = Attribute("data-" + name, value)

  def onInput[M](msg: String => M): Attr[M] =
    onEvent("input", (e: Tyrian.Event) => msg(e.target.asInstanceOf[Tyrian.HTMLInputElement].value))

  def onChange[M](msg: String => M): Attr[M] =
    onEvent("change", (e: Tyrian.Event) => msg(e.target.asInstanceOf[Tyrian.HTMLInputElement].value))

  def style(name: String, value: String): Attr[Nothing] = Attribute("style", Style(name, value).toString)
  @targetName("style_Style")
  def style(style: Style): Attr[Nothing]    = Attribute("style", style.toString)
  def styles(styles: Style*): Attr[Nothing] = Attribute("style", Style.combineAll(styles.toList).toString)
  @targetName("style_tuples")
  def styles(styles: (String, String)*): Attr[Nothing] =
    Attribute("style", Style.combineAll(styles.map(p => Style(p._1, p._2)).toList).toString)

end Html

/** Object used to provide SVG syntax `import tyrian.SVG.*`
  */
object SVG extends SVGTags with SVGAttributes

/** Object used to provide Aria attribute syntax `import tyrian.Aria.*`
  */
object Aria extends AriaAttributes

/** An HTML tag */
final case class Tag[+M](name: String, attributes: List[Attr[M]], children: List[Elem[M]]) extends Html[M]:
  def map[N](f: M => N): Tag[N] =
    this.copy(
      attributes = attributes.map(_.map(f)),
      children = children.map(_.map(f))
    )

  def innerHtml(html: String): RawTag[M] =
    RawTag(name, attributes, html)

/** An HTML tag with raw HTML rendered inside. Beware that the inner HTML is not validated to be correct, nor does it
  * get modified as a response to messages in any way.
  */
final case class RawTag[+M](name: String, attributes: List[Attr[M]], innerHTML: String) extends Html[M]:
  def map[N](f: M => N): RawTag[N] =
    this.copy(
      attributes = attributes.map(_.map(f))
    )

  def innerHtml(html: String): RawTag[M] =
    RawTag(name, attributes, html)
