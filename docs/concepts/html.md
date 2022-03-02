---
title: "Rendering HTML"
menuTitle: "Rendering HTML"
---

## HTML syntax

In Tyrian, you describe your view in Scala and the VirtualDom implementation that powers Tyrian ([Snabbdom](https://github.com/snabbdom/snabbdom)) renders that description into HTML.

Here is a simple made up example of the syntax to give you a flavor:

```scala mdoc:silent
import tyrian.*
import tyrian.Html.*

val styles  = style(CSS.`font-family`("Arial, Helvetica, sans-serif"))
val topLine = p(b(text("This is some HTML in bold.")))

div(id := "my-container")(
  div(styles)(
    topLine,
    p("Hello, world!"),
    button(onClick(Msg.Greet))("Say hello!")
  )
)
```

### Working with Tags, Attributes and Properties

A vast list of the common HTML tags, attributes, and CSS property types have been generated for you, and effort has gone in to making the experience reasonably pleasant.

The arrangement of tags follows similar approaches by other compiled languages trying to represent HTML:

`tag-name(List(attributes, ...))(List(children, ...))`

For example, here we have a `span` with an attribute and a child element:

```scala
span(`class` := "green-box")(
  p("This is some text.")
)
```

You can omit the attributes and the syntax is valid:

```scala
span(
  p("This is some text.")
)
```

Note that plain text can be declare as `text` or just omitted, in other words these are equivalent:

```scala
p("some text")
p(text("some text"))
```

To distinguish them from similarly named tags (e.g. the `title` attribute and the `title` tag...), attributes are declared as `attribute-name := attribute-value`, e.g.:

```scala
id := "my-container"
```

Some HTML attributes / properties use Scala reserved words, and so have a variety of encodings to suit all tastes. For example `class` is a Scala reserved word and you cannot use it directly, but you can use any of these instead:

```text
`class`
cls
className
_class
```

Styles are also baked in, albeit it in a slightly crude way, but you will get some IDE support. You can do things like the following:

```scala
p(CSS.`font-weight`("bold"))("Hello")
```

### Rolling your own

If you find we've missed a tag or attribute or something, [please raise an issue](https://github.com/PurpleKingdomGames/tyrian/issues). In the meantime, you can always make your own. Here are just a few made up examples, each of these has numerous constructors for you to explore:

```scala
// A 'canvas' tag
tag("canvas", List(id := "an-id"), Nil)

// An attribute
attribute("my-attribute", "its-value")

// A property
property("my-property", "its-value")

// Styles
style("width", "100px")
styles("width" -> "100px", "height" -> "50px")

// An event-type attribute
onEvent("click", (evt: Tyrian.Event) => Msg.DoSomething)
```

> Note that everything is stringly typed, and that's because in HTML everything is stringly typed too! In the generated tags, we've added support for things like attributes accepting `Int`s and `Boolean`s and so on where they have known acceptable input types.

#### Known Gotcha: Attributes vs. Properties

One occasion where you may need to make your own tags or attributes etc., is when Tyrian has an incorrectly declared definition of something or other.

A previous case of this was where the input field `value` `property` was incorrectly declared as an `attribute`, and so the value wasn't changed as expected based on model updates.

Effort has gone in to getting these things right, but if you come across any issues, [please report them](https://github.com/PurpleKingdomGames/tyrian/issues), and the workaround is to re-declare it yourself as above while a fix is produced.
