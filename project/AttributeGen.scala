import sbt._
import scala.sys.process._

object AttributeGen {

  def genAttr(tag: AttributeType, isAttribute: Boolean): String =
    tag match {
      case Normal(name, attrName, types) if isAttribute => genNormal(name, attrName, types)
      case Normal(name, attrName, types)                => genNormalProp(name, attrName, types)
      case NoValue(name, attrName)                      => genNoValue(name, attrName)
      case EventEmitting(name, attrName, eventType)     => genEventEmitting(name, attrName, eventType)
    }

  def genNormal(attrName: String, realName: Option[String], types: List[String]): String = {
    val attr = realName.getOrElse(attrName.toLowerCase)
    val res = types.map { t =>
      s"""  @targetName("$attrName-$t")
      |  val $attrName: AttributeName$t = AttributeName$t("$attr")
      |""".stripMargin
    }.mkString

    "\n" + res + "\n"
  }

  def genNormalProp(propName: String, realName: Option[String], types: List[String]): String = {
    val prop = realName.getOrElse(propName.toLowerCase)
    val res = types.map { t =>
      s"""  @targetName("$propName-$t")
      |  val $propName: PropertyName$t = PropertyName$t("$prop")
      |""".stripMargin
    }.mkString

    "\n" + res + "\n"
  }

  def genNoValue(attrName: String, realName: Option[String]): String = {
    val attr = realName.getOrElse(attrName.toLowerCase)
    s"""  def $attrName: NamedAttribute = NamedAttribute("$attr")
    |  def $attrName(isUsed: Boolean): Attr[Nothing] = if isUsed then NamedAttribute("$attr") else EmptyAttribute
    |
    |""".stripMargin
  }

  def genEventEmitting(attrName: String, realName: Option[String], eventType: Option[String]): String = {
    val attr = realName.getOrElse {
      val n = attrName.toLowerCase
      if (n.startsWith("on")) n.substring(2)
      else n
    }
    eventType match {
      case Some(evt) =>
        s"""  def $attrName[M](msg: Tyrian.$evt => M): Event[Tyrian.$evt, M] = AttributeSyntax.onEvent("$attr", msg)
        |
        |""".stripMargin

      case None =>
        s"""  def $attrName[M](msg: M): Event[Tyrian.Event, M] = AttributeSyntax.onEvent("$attr", (_: Tyrian.Event) => msg)
        |
        |""".stripMargin
    }
  }

  def template(moduleName: String, fullyQualifiedPath: String, contents: String): String =
    s"""package $fullyQualifiedPath
    |
    |import scala.annotation.targetName
    |
    |// GENERATED by AttributeGen.scala - DO NOT EDIT
    |trait $moduleName {
    |
    |$contents
    |
    |}
    """.stripMargin

  def gen(fullyQualifiedPath: String, sourceManagedDir: File): Seq[File] =
    Seq(htmlAttrsList, svgAttrsList, ariaAttrsList).map { case AttributesList(attrs, props, name) =>
      val file: File =
        sourceManagedDir / s"$name.scala"

      if (!file.exists()) {
        println("Generating Html Attributes")

        val contents: String =
          "\n\n  // Attributes\n\n" +
            attrs.map(a => genAttr(a, true)).mkString +
            "\n\n  // Properties\n\n" +
            props.map(p => genAttr(p, false)).mkString

        val newContents: String =
          template(name, fullyQualifiedPath, contents)

        IO.write(file, newContents)

        println("Written: " + file.getCanonicalPath)
      }

      file
    }

  def htmlAttrs: List[AttributeType] =
    List(
      Normal("accept"),
      Normal("accessKey"),
      Normal("action"),
      Normal("alt"),
      NoValue("async"),
      Normal("autoComplete"),
      NoValue("autoFocus"),
      NoValue("autofocus"),
      NoValue("autoPlay"),
      NoValue("autoplay"),
      Normal("charset"),
      NoValue("checked"), // property
      Normal("cite"),
      Normal("`class`", "class"),
      Normal("cls", "class"),
      Normal("className", "class"),
      Normal("classname", "class"),
      Normal("_class", "class"),
      Normal("cols").withTypes("String", "Int"),
      Normal("colSpan").withTypes("String", "Int"),
      Normal("colspan").withTypes("String", "Int"),
      Normal("content"),
      Normal("contentEditable").withTypes("String", "Boolean"),
      Normal("contenteditable").withTypes("String", "Boolean"),
      NoValue("controls"),
      Normal("coords"),
      Normal("data"),
      Normal("dateTime"),
      Normal("datetime"),
      NoValue("default"),
      NoValue("defer"),
      Normal("dir"),
      Normal("dirname"),
      NoValue("disabled"),
      NoValue("download"),
      Normal("draggable").withTypes("String", "Boolean"),
      Normal("encType"),
      Normal("enctype"),
      Normal("_for", "for"),
      Normal("`for`", "for"),
      Normal("forId", "for"),
      Normal("htmlFor", "for"),
      Normal("form"),
      Normal("formAction"),
      Normal("formaction"),
      Normal("headers"),
      Normal("height").withTypes("String", "Int"),
      NoValue("hidden"),
      Normal("high").withTypes("String", "Double"),
      Normal("href"),
      Normal("hrefLang"),
      Normal("hreflang"),
      Normal("http"),
      Normal("id"),
      NoValue("isMap"),
      NoValue("ismap"),
      Normal("kind"),
      Normal("label"),
      Normal("lang"),
      Normal("list"),
      NoValue("loop"),
      Normal("low").withTypes("String", "Double"),
      Normal("max").withTypes("String", "Int"),
      Normal("maxLength").withTypes("String", "Int"),
      Normal("maxlength").withTypes("String", "Int"),
      Normal("media"),
      Normal("method"),
      Normal("min").withTypes("String", "Int"),
      Normal("multiple"),
      Normal("muted"),
      Normal("name"),
      NoValue("noValidate"),
      NoValue("novalidate"),
      EventEmitting("onAbort"),
      EventEmitting("onAfterPrint"),
      EventEmitting("onBeforePrint"),
      EventEmitting("onBeforeUnload"),
      EventEmitting("onBlur"),
      EventEmitting("onCanPlay"),
      EventEmitting("onCanPlayThrough"),
      // EventEmitting("onChange", "change"), // Provided manually as it doesn't fit the pattern
      EventEmitting("onClick", "click"),
      EventEmitting("onContextMenu"),
      EventEmitting("onCopy"),
      EventEmitting("onCueChange"),
      EventEmitting("onCut"),
      EventEmitting("onDblClick"),
      EventEmitting("onDoubleClick", "dblclick"),
      EventEmitting("onDrag"),
      EventEmitting("onDragEnd"),
      EventEmitting("onDragEnter"),
      EventEmitting("onDragLeave"),
      EventEmitting("onDragOver"),
      EventEmitting("onDragStart"),
      EventEmitting("onDrop"),
      EventEmitting("onDurationChange"),
      EventEmitting("onEmptied"),
      EventEmitting("onEnded"),
      EventEmitting("onError"),
      EventEmitting("onFocus"),
      EventEmitting("onHashChange"),
      // EventEmitting("onInput"), // Provided manually as it doesn't fit the pattern
      EventEmitting("onInvalid"),
      EventEmitting("onKeyDown"),
      EventEmitting("onKeyDown", None, Option("KeyboardEvent")),
      EventEmitting("onKeyPress"),
      EventEmitting("onKeyPress", None, Option("KeyboardEvent")),
      EventEmitting("onKeyUp"),
      EventEmitting("onKeyUp", None, Option("KeyboardEvent")),
      EventEmitting("onLoad"),
      EventEmitting("onLoadedData"),
      EventEmitting("onLoadedMetadata"),
      EventEmitting("onLoadStart"),
      EventEmitting("onMouseDown"),
      EventEmitting("onMouseDown", None, Option("MouseEvent")),
      EventEmitting("onMouseMove"),
      EventEmitting("onMouseMove", None, Option("MouseEvent")),
      EventEmitting("onMouseOut"),
      EventEmitting("onMouseOut", None, Option("MouseEvent")),
      EventEmitting("onMouseOver"),
      EventEmitting("onMouseOver", None, Option("MouseEvent")),
      EventEmitting("onMouseEnter"),
      EventEmitting("onMouseEnter", None, Option("MouseEvent")),
      EventEmitting("onMouseLeave"),
      EventEmitting("onMouseLeave", None, Option("MouseEvent")),
      EventEmitting("onMouseUp"),
      EventEmitting("onMouseUp", None, Option("MouseEvent")),
      EventEmitting("onMouseWheel"),
      EventEmitting("onMouseWheel", None, Option("MouseEvent")),
      EventEmitting("onOffline"),
      EventEmitting("onOnline"),
      EventEmitting("onPageHide"),
      EventEmitting("onPageShow"),
      EventEmitting("onPaste"),
      EventEmitting("onPause"),
      EventEmitting("onPlay"),
      EventEmitting("onPlaying"),
      EventEmitting("onPopState"),
      EventEmitting("onProgress"),
      EventEmitting("onRateChange"),
      EventEmitting("onReset"),
      EventEmitting("onResize"),
      EventEmitting("onScroll"),
      EventEmitting("onSearch"),
      EventEmitting("onSeeked"),
      EventEmitting("onSeeking"),
      EventEmitting("onSelect"),
      EventEmitting("onStalled"),
      EventEmitting("onStorage"),
      EventEmitting("onSubmit"),
      EventEmitting("onSuspend"),
      EventEmitting("onTimeUpdate"),
      EventEmitting("onToggle"),
      EventEmitting("onUnload"),
      EventEmitting("onVolumeChange"),
      EventEmitting("onWaiting"),
      EventEmitting("onWheel"),
      NoValue("open"),
      Normal("optimum").withTypes("String", "Double"),
      Normal("pattern"),
      Normal("placeholder"),
      Normal("poster"),
      Normal("preload"),
      NoValue("readOnly"),
      NoValue("readonly"),
      Normal("rel"),
      NoValue("required"),
      NoValue("reversed"),
      Normal("role"),
      Normal("rows").withTypes("String", "Int"),
      Normal("rowSpan").withTypes("String", "Int"),
      Normal("rowspan").withTypes("String", "Int"),
      NoValue("sandbox"),
      Normal("scope"),
      NoValue("selected"), // property
      Normal("shape"),
      Normal("size").withTypes("String", "Int"),
      Normal("sizes"),
      Normal("span").withTypes("String", "Int"),
      Normal("spellCheck").withTypes("String", "Boolean"),
      Normal("spellcheck").withTypes("String", "Boolean"),
      Normal("src"),
      Normal("srcDoc"),
      Normal("srcdoc"),
      Normal("srcLang"),
      Normal("srclang"),
      Normal("srcSet"),
      Normal("srcset"),
      Normal("start").withTypes("String", "Int"),
      Normal("step").withTypes("String", "Int"),
      Normal("style").withTypes("String", "Style"),
      Normal("tabIndex").withTypes("String", "Int"),
      Normal("tabindex").withTypes("String", "Int"),
      Normal("target"),
      Normal("title"),
      Normal("translate"),
      Normal("`type`", "type"),
      Normal("_type", "type"),
      Normal("typ", "type"),
      Normal("tpe", "type"),
      Normal("useMap"),
      Normal("usemap"),
      Normal("width").withTypes("String", "Int"),
      Normal("wrap")
    )

  def htmlProps: List[Normal] =
    List(
      Normal("checked").withTypes("Boolean"),
      Normal("indeterminate").withTypes("Boolean"),
      Normal("selected").withTypes("Boolean"),
      Normal("value").withTypes("String")
    )

  def svgAttrs: List[AttributeType] = List(
    Normal("cx"),
    Normal("cy"),
    Normal("d"),
    Normal("fill"),
    Normal("pathLength", "pathLength"), // svg attributes are case sensitive
    Normal("points"),
    Normal("r"),
    Normal("rx"),
    Normal("ry"),
    Normal("stroke"),
    Normal("viewBox", "viewBox"), // svg attributes are case sensitive
    Normal("xmlns"),
    Normal("x"),
    Normal("x1"),
    Normal("x2"),
    Normal("y"),
    Normal("y1"),
    Normal("y2")
  )

  def ariaAttrs: List[AttributeType] = List(
    Normal("ariaAutocomplete", "aria-autocomplete"),
    Normal("ariaChecked", "aria-checked"),
    Normal("ariaDisabled", "aria-disabled"),
    Normal("ariaErrorMessage", "aria-errormessage"),
    Normal("ariaExpanded", "aria-expanded"),
    Normal("ariaHasPopup", "aria-haspopup"),
    Normal("ariaHidden", "aria-hidden"),
    Normal("ariaInvalid", "aria-invalid"),
    Normal("ariaLabel", "aria-label"),
    Normal("ariaLevel", "aria-level"),
    Normal("ariaModal", "aria-modal"),
    Normal("ariaMultiline", "aria-multiline"),
    Normal("ariaMultiselectable", "aria-multiselectable"),
    Normal("ariaOrientation", "aria-orientation"),
    Normal("ariaPlaceholder", "aria-placeholder"),
    Normal("ariaPressed", "aria-pressed"),
    Normal("ariaReadOnly", "aria-readonly"),
    Normal("ariaRequired", "aria-required"),
    Normal("ariaSelected", "aria-selected"),
    Normal("ariaSort", "aria-sort"),
    Normal("ariaValueMax", "aria-valuemax"),
    Normal("ariaValueMin", "aria-valuemin"),
    Normal("ariaValueNow", "aria-valuenow"),
    Normal("ariaValueText", "aria-valuetext"),
    Normal("ariaBusy", "aria-busy"),
    Normal("ariaLive", "aria-live"),
    Normal("ariaRelevant", "aria-relevant"),
    Normal("ariaAtomic", "aria-atomic"),
    Normal("ariaDropEffect", "aria-dropeffect"),
    Normal("ariaGrabbed", "aria-grabbed"),
    Normal("ariaActiveDescendant", "aria-activedescendant"),
    Normal("ariaColCount", "aria-colcount"),
    Normal("ariaColIndex", "aria-colindex"),
    Normal("ariaColSpan", "aria-colspan"),
    Normal("ariaControls", "aria-controls"),
    Normal("ariaDescribedBy", "aria-describedby"),
    Normal("ariaDescription", "aria-description"),
    Normal("ariaDetails", "aria-details"),
    Normal("ariaFlowTo", "aria-flowto"),
    Normal("ariaLabelledBy", "aria-labelledby"),
    Normal("ariaOwns", "aria-owns"),
    Normal("ariaPosInset", "aria-posinset"),
    Normal("ariaRowCount", "aria-rowcount"),
    Normal("ariaRowIndex", "aria-rowindex"),
    Normal("ariaRowSpan", "aria-rowspan"),
    Normal("ariaSetSize", "aria-setsize")
  )

  def htmlAttrsList: AttributesList = AttributesList(htmlAttrs, htmlProps, "HtmlAttributes")
  def svgAttrsList: AttributesList  = AttributesList(svgAttrs, List(), "SVGAttributes")
  def ariaAttrsList: AttributesList = AttributesList(ariaAttrs, List(), "AriaAttributes")

}

sealed trait AttributeType
final case class Normal(name: String, attrName: Option[String], types: List[String]) extends AttributeType {
  def withTypes(newTypes: String*): Normal =
    Normal(name, attrName, newTypes.toList)
}
object Normal {
  def apply(name: String): Normal                   = Normal(name, None, List("String"))
  def apply(name: String, attrName: String): Normal = Normal(name, Some(attrName), List("String"))
}
final case class NoValue(name: String, attrName: Option[String]) extends AttributeType
object NoValue {
  def apply(name: String): NoValue                   = NoValue(name, None)
  def apply(name: String, attrName: String): NoValue = NoValue(name, Some(attrName))
}
final case class EventEmitting(name: String, attrName: Option[String], eventType: Option[String]) extends AttributeType
object EventEmitting {
  def apply(name: String): EventEmitting                   = EventEmitting(name, None, None)
  def apply(name: String, attrName: String): EventEmitting = EventEmitting(name, Some(attrName), None)
}

final case class AttributesList(attrs: List[AttributeType], props: List[AttributeType], namespace: String)
