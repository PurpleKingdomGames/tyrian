package example

import tyrian.*

enum AppEvent extends GlobalMsg:
  case FollowLink(href: String)
  case NoOp
