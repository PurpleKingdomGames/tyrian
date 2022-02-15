---
title: "Building & Bundling"
menuTitle: "Bundling"
---

The JavaScript community don't like to use the word 'compiler', but tools that gather, parse, and link source files into deployable 'bundles' are an essential part of modern web app development.

These tools are called 'bundlers', the most famous of which is probably [Webpack](https://webpack.js.org/). It is beyond the scope of these docs to explain how bundlers work, but they are well documented tools.

Tyrian needs bundlers too. We could probably manage without them just for our raw app, but inevitably we'll need images, and style sheets, and font files, and other JS libraries and so on. Bundlers also give you access to things like dev-servers that feature hot-reloading (i.e. your website refreshes when you re-compile your Tyrian app) which is great during development.

I've already mentioned that Webpack is the most well know bundler, and Scala.js has an [sbt based bundler](https://scalacenter.github.io/scalajs-bundler/) that uses Webpack under the covers. We have a standalone [example of using Tyrian with scalajs-bundler](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples/bundler) you can look at, and we also use it in our [server based example](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples/server-examples).

[All of the other examples](https://github.com/PurpleKingdomGames/tyrian/tree/main/examples) use [Parcel.js](https://parceljs.org/). This is because Webpack is very config heavy and scary, while Parcel is almost zero config.

Neither solution is perfect. Use whatever suits your needs best.
