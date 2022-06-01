---
title: "Building & Bundling"
menuTitle: "Bundling"
---

The JavaScript community don't like to use the word 'compiler', but tools that gather, parse, and link source files into deployable 'bundles' are an essential part of modern web app development.

These tools are called 'bundlers', the most well known of which is probably [Webpack](https://webpack.js.org/). It is beyond the scope of these docs to explain how bundlers work, but they are well documented tools.

Tyrian needs bundlers too. We could probably manage without them for our raw app, but sooner or later we're bound to need images, and style sheets, and font files, and other JS libraries etc., and that's where bundlers shine.

Bundlers also give you access to things like dev-servers that feature hot-reloading (i.e. your website refreshes when you re-compile your Tyrian app) which is great during development.

## Bundler differences

### Parcel.js

[Most of our examples](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples) use [Parcel.js](https://parceljs.org/). This is because Webpack is very config heavy, while Parcel is almost zero config. In practical terms this means that Parcel is very quick to get up and running, but if you find a corner case then sourcing a workaround can be tricky. Webpack on the other hand looks to have configuration and plugins for pretty much anything you can think of, and an increased learning curve to go with it.

### scalajs-bundler

I've already mentioned that Webpack is the most well known bundler, and Scala.js has an sbt based bundler called [scalajs-bundler](https://scalacenter.github.io/scalajs-bundler/) that uses Webpack under the covers.

We have a standalone [example of using Tyrian with scalajs-bundler](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples/bundler) you can look at, and we also use it in our [server based example](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples/server-examples).

#### Converting from Parcel.js to scalajs-bundler

In the parcel.js examples, you always call `TyrianApp.launch(..)` sooner or later. With scalajs-bundler (at least in our examples), the app launches itself. There are a few subtle differences you need to look out for:

1. In your `build.sbt` file, you need to tell Scala.js to run from the `main` method.

`scalaJSUseMainModuleInitializer := true,`

2. ...and so you'll need a `main` method!

To do that we just call one of the underlying `launch` methods on the `TyrianApp` trait. (These are the same methods we would have called from JavaScript to launch the app via Parcel.js.)

```scala
def main(args: Array[String]): Unit =
  launch("myapp")
```

3. Add the scalajs-bundler plugin.

Add the following to `project/plugins.sbt`:

```scala
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
```

And enable the plugin on the project in your `build.sbt` file:

```scala
enablePlugins(ScalaJSBundlerPlugin)
```

4. Reference the right `js` file.

In the parcel.js examples, you'll see that we directly reference the normal Scala.js build output file in the `target` directory (or `out` directory in Mill examples), such as `target/@SCALA_VERSION@/tyrianapp-fastopt.js`. This is so that you can hot-reload on re-compile. However in scalajs-bundler, the output folder and file name you need to use will be slightly different, such as `target/@SCALA_VERSION@/scalajs-bundler/main/tyrianapp-fastopt-bundle.js`.

This is because scalajs-bundler is combining your code with all your other js dependencies into one big bundle, hence the name change.

> Note: Behind the scenes, parcel.js does the same thing, it's just not as obvious and doesn't require you to do anything.
