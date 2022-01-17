package tyrian.runtime

import tyrian._

object TyrianSSR:

  private val docType: String = "<!DOCTYPE HTML>"

  import Render.*

  def render[Model, Msg](includeDocType: Boolean, model: Model, view: Model => Html[Msg]): String =
    if includeDocType then docType + view(model).render else view(model).render
  def render[Model, Msg](model: Model, view: Model => Html[Msg]): String =
    render(false, model, view)

  def render[Model, Msg](includeDocType: Boolean, html: Html[Msg]): String =
    if includeDocType then docType + html.render else html.render
  def render[Model, Msg](html: Html[Msg]): String =
    render(false, html)

  def render[Model, Msg](includeDocType: Boolean, elems: List[Elem[Msg]]): String =
    if includeDocType then docType + elems.map(_.render).mkString else elems.map(_.render).mkString
  def render[Model, Msg](elems: List[Elem[Msg]]): String =
    render(false, elems)
  def render[Model, Msg](includeDocType: Boolean, elems: Elem[Msg]*): String =
    render(includeDocType, elems.toList)
  def render[Model, Msg](elems: Elem[Msg]*): String =
    render(elems.toList)

object Render:

  val spacer = (str: String) => if str.isEmpty then str else " " + str

  extension [Msg](elem: Elem[Msg])
    def render: String =
      elem match
        case t: Text    => t.value
        case h: Html[_] => h.render

  extension [Msg](html: Html[Msg])
    def render: String =
      html match
        case tag: Tag[_] =>
          val attributes =
            spacer(tag.attributes.map(_.render).mkString(" "))

          val children = tag.children.map {
            case t: Text    => t.value
            case h: Html[_] => h.render
          }.mkString

          s"""<${tag.name}$attributes>$children</${tag.name}>"""

  extension (a: Attr[_])
    def render: String =
      a match
        case _: Event[_, _] => ""
        case a: Attribute   => a.render
        case p: Property    => p.render

  extension (a: Attribute)
    def render: String =
      s"""${a.name}="${a.value}""""

  extension (p: Property)
    def render: String =
      s"""${p.name}="${p.value}""""
