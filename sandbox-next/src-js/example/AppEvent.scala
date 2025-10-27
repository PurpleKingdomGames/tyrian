package example

import tyrian.next.*

enum AppEvent extends GlobalMsg:
  case FollowLink(href: String)
  case NoOp
