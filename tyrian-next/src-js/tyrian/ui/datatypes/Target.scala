package tyrian.ui.datatypes

import tyrian.next.GlobalMsg

enum Target derives CanEqual:
  case Blank, Self, Parent, TOP

  def toAttributeValue: String =
    this match
      case Target.Blank  => "_blank"
      case Target.Self   => "_self"
      case Target.Parent => "_parent"
      case Target.TOP    => "_top"

  def toAttribute: tyrian.Attr[GlobalMsg] =
    tyrian.Html.target := toAttributeValue
