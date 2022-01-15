package tyrian.runtime

import tyrian._

object TyrianSSR:

  import Render.*

  def render[Model, Msg](model: Model, view: Model => Html[Msg]): String =
    view(model).render

  def render[Model, Msg](html: Html[Msg]): String =
    html.render

object Render:

  val spacer = (str: String) => if str.isEmpty then str else " " + str

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
