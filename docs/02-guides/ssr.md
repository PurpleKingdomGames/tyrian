# Server-side Rendering & HTMX

Tyrian's tag syntax can also be used in your JVM server side projects, for delivering re-rendered HTML to the frontend. This allows you to use the same syntax on both the client and server-sides of your application.

As well as supporting normal HTML syntax, Tyrian has an additional module to support [HTMX](https://htmx.org/) syntax, for rendering [Hypermedia](https://en.wikipedia.org/wiki/Hypermedia) applications.

To use Tyrian tags in a JVM project, include the JVM Tyrian library as follows:

```scala
libraryDependencies ++= Seq(
  "io.indigoengine" %% "tyrian" % "@VERSION@"
)
```

Then bring in the following imports:

```scala
import tyrian.*
import tyrian.Html.*
```

For HTMX, it's a similar arrangement:

```scala
libraryDependencies ++= Seq(
  "io.indigoengine" %% "tyrian-htmx" % "@VERSION@"
)
```

With the following imports:

```scala
import tyrian.htmx.*
import tyrian.htmx.Html.*
```

You will also need to embed the HTMX JavaScript into your page. If you use Tyrian's syntax, it looks like this:

```scala
html(
  head(
    script(src := "https://unpkg.com/htmx.org@1.9.10")(),
    script(src := "https://unpkg.com/htmx.org/dist/ext/ws.js")()
  )
)
```

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

## What is HTMX?

From the [HTMX](https://htmx.org/) homepage:

> htmx gives you access to AJAX, CSS Transitions, WebSockets and Server Sent Events directly in HTML, using attributes, so you can build modern user interfaces with the simplicity and power of hypertext.

In other words, it allows you to elegantly build web applications where the logic lives on the server, rather than in the client. This is known as [Hypermedia](https://en.wikipedia.org/wiki/Hypermedia), and is remaniscent of how the web worked cicra 2000, but with much better tooling and more modern practices!

Please refer to the [HTMX documentation](https://htmx.org/docs/) for guides on how to use it.

### How does HTMX fit in with Tyrian?

In simple terms, it doesn't! They are two totally different models and philosophies for building interactive web pages.

You may well wonder why we've added support for a competing library, then?

Well, there are a few reasons, but the main one is that sometimes Tyrian (or React, or Laminar, or any other client side SPA framework you care to name) is just too much. Sometimes, all you want to do is add a contact form to your otherwise static website, or serve a quick admin page with a few simple elements on it*. In those situations, building a full SPA is overkill.

As with all tools, there is a time and a place for Tyrian (full SPA's with complex client side logic) and a time for HTMX (or similar, where the client experience is simpler and the server-side can and should sensibly take the burden).

The view, at least at the time of writing, is that HTMX fills a nice solution gap between ordinary HTML, and a full SPA. As long as you're writing HTMX for / in Scala, you might as well have IDE support, and if you're going to do all that, we might as well enable you to use Tyrian's markup to do the work.

Happy Hypermedia'ing!

(* HTMX can do a lot more than that, it's just an example, please refer to their docs.)
