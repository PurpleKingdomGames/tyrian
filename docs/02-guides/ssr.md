# Server-side Rendering

## What is server-side rendering? (SSR)

These days, a normal straight-forward relationship between a frontend client and some sort of backend service, is that the client makes an HTTP request to the service, and receives some data back that it uses to render a page/view for the user. But it wasn't always so!

Back in the days when Perl dominated the world of server side technology and ASP was new and exciting, it was more normal for the browser to hit a url, and for the page rendering to happen entirely on the server: The HTML delivered whole and fully formed to the browser. This allow the pages to be built up from data pulled from other services and databases.

This arrangement has come back into fashion, and now has the fancy name "server-side rendering" or SSR. The benefit of SSR is that HTML is incredibly cache-able. If you can manufacture a page or a fragment of a page once, then you may be able to cache it for super fast page loading performance.

## Simple SSR with Tyrian

Below is a simple example of SRR with Tyrian:

```scala mdoc:js
import tyrian.*
import tyrian.Html.*

val styles  = style(CSS.`font-family`("Arial, Helvetica, sans-serif"))
val topLine = p(b(text("HTML fragment rendered by Tyrian on the server.")))

val output: String =
  div(styles)(
    topLine,
    p("Hello, world!")
  ).render
```

As you can see, this is completely ordinary Scala, which means you can do anything that Scala lets you do in order to generate this HTML block, without having to learn a templating language like Mustache.

The `.render` extension method is not strictly necessary since this is now the behaviour of calling `.toString` on a tag.

There is an example of SRR in the [server-examples](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples).
