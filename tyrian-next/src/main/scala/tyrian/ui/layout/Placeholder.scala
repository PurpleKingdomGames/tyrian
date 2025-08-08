// package tyrian.ui.layout

// import tyrian.next.GlobalMsg
// import tyrian.next.Marker
// import tyrian.next.MarkerId
// import tyrian.ui.Theme
// import tyrian.ui.UIElement

// final case class Placeholder(
//     id: MarkerId,
//     children: List[UIElement[?, GlobalMsg]],
//     classNames: Set[String],
//     _modifyTheme: Option[Theme => Theme]
// ) extends UIElement[Placeholder, GlobalMsg]:

//   def withChildren(children: UIElement[?, GlobalMsg]*): Placeholder =
//     this.copy(children = children.toList)

//   def addChild(child: UIElement[?, GlobalMsg]): Placeholder =
//     this.copy(children = children :+ child)

//   def withClassNames(classes: Set[String]): Placeholder =
//     this.copy(classNames = classes)

//   def modifyTheme(f: Theme => Theme): Placeholder =
//     this.copy(_modifyTheme = Some(f))

//   def toHtml: Theme ?=> tyrian.Html[GlobalMsg] =
//     Placeholder.toHtml(this)

// object Placeholder:

//   def apply(id: MarkerId): Placeholder =
//     Placeholder(id, Nil, Set(), None)

//   def apply(id: MarkerId, children: UIElement[?, GlobalMsg]*): Placeholder =
//     Placeholder(id, children.toList, Set(), None)

//   def toHtml(element: Placeholder)(using theme: Theme): tyrian.Html[GlobalMsg] =
//     val t = element._modifyTheme match
//       case Some(f) => f(theme)
//       case None    => theme

//     val htmlChildren = element.children.map(_.toHtml(using t))
    
//     Marker(element.id, htmlChildren).asInstanceOf[tyrian.Html[GlobalMsg]]
