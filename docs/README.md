# Tyrian

Tyrian is primarily an Elm-inspired, purely functional UI library for Scala 3.

Its purpose is to make building highly interactive websites in Scala 3 fun! Tyrian allows you to describe web pages and complex interactions in a way that is elegant, easy to read and easy to reason about.

Tyrian is not designed to work alone, the intention is that you will use it alongside a web bundler to look after things like your media assets and stylesheets. (All our examples use [Parcel](https://parceljs.org/).)

## Tyrian 💜's Indigo

Tyrian is by the maintainers of [Indigo](https://indigoengine.io/), a Scala 3 game engine. As such, Indigo web games are easily embedded into Tyrian apps, allowing easy embedding and seamless communication between their respective event/messaging systems (via the bridge module).

## When should I consider using Tyrian?

Tyrian is designed for building SPAs (Single Page Applications): Web pages with lots of interactive elements.

One use case we're particularly excited about is augmenting Indigo games with rich HTML UI elements.

You can also use Tyrian to do Server-side Rendering (SSR) in conjunction with your favorite Scala HTTP framework in place of, say, a templating library.

## What should I avoid using Tyrian for?

Tyrian's frontend framework is not a great candidate for static websites - like this one! - where the content is there to be read, not interacted / played / engaged with.

Tyrian's SSR and [HTMX](https://htmx.org/) support offers a middle ground between static HTML and full blown SPAs.

## Why shouldn't I just use some Scala.js-React-based-thingy instead?

In fact there are a good number of well made Scala.js alternatives to Tyrian, including:

- [Laminar](https://laminar.dev/)
- [Calico](https://github.com/armanbilge/calico)
- [Outwatch](https://github.com/outwatch/outwatch)
- [scalajs-react](https://github.com/japgolly/scalajs-react)
- [Slinky](https://slinky.dev/)

Functional programmers may prefer Laminar or Calico, which are based on a Functional Reactive Programming (FRP) model that is quite different to the way Tyrian works.

React is an industry standard framework these days, and so there are lots and lots of resources out there to help you use it.

Tyrian works fundamentally differently to those other frameworks. In the authors very opinionated opinion: The Elm Architecture upon which it is based, is the most productive, fun, and _sane_ frontend architectural approach there is.

This library exists because we don't want to work any other way. We encourage you to try a few different approaches and decide what works best for you. 😊
