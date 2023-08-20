# Frontend Routing

All Tyrian apps need to implement a routing function:

```scala
def router: Location => Msg
```

There are a few ways to implement the `router` function. Please note that in all cases, you are told if the link is considered internal or external and they are treated separately. 

If your needs are simple, then the easiest way to implement the router is to use the helpers in the `Routing` module. For example, this one simply forwards on the `href`s as custom `Msg`s. As usual, note that you need to provide suitable `Msg` types:

```scala
def router: Location => Msg = Routing.basic(Msg.FollowInternalLink(_), Msg.FollowExternalLink(_))
```

There are also `Routing.externalOnly` and `Routing.none` variations.

Alternatively, you can do a full route match, like this:

```scala
  def router: Location => Msg =
    case loc: Location.Internal =>
      loc.pathName match
        case "/"      => Msg.NavigateTo(Page.Page1)
        case "/page1" => Msg.NavigateTo(Page.Page1)
        case "/page2" => Msg.NavigateTo(Page.Page2)
        case "/page3" => Msg.NavigateTo(Page.Page3)
        case "/page4" => Msg.NavigateTo(Page.Page4)
        case "/page5" => Msg.NavigateTo(Page.Page5)
        case "/page6" => Msg.NavigateTo(Page.Page6)
        case _        => Msg.NoOp

    case loc: Location.External =>
      Msg.NavigateToUrl(loc.href)
```

The `Location` type comes with all sorts of information in it for you to decide how to route, it is similar to the JavaScript `Location` type.

Once you've done your routing, you'll want to use some of the new `Nav` cmds. 

Internal links from anchor tags will automatically update the address bar, but for internal links that are not from anchor tags, you can use `Nav.pushUrl` to update the address bar (this does _not_ tell the browser to change locations, it just adjusts the history and what is displayed in the address bar). For external links, you can use `Nav.loadUrl` to tell the browser to follow the link.

Happy routing!
