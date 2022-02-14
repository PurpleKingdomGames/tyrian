+++
title = "Information & Trivia"
menuTitle = "Information"
weight = 4
+++

## Discussion

If you're new to the territory, we highly recommend [this thoughtful blog post](https://dev.to/raquo/my-four-year-quest-for-perfect-scala-js-ui-development-b9a) by [Laminar](https://laminar.dev/)'s author, Nikita Gazarov.

There is a point in that post where Nikita says the following:

> (..) and having now walked the path myself I finally understood exactly what that reason was: functional reactive programming (FRP) and virtual DOM don't mix!
> 
> Virtual DOM and FRP solve the exact same problem – efficiently keeping the rendered DOM in sync with application state – but they approach it from entirely opposite directions (..)

Quite right too. It's an important fork in the road. One direction takes you to FRP and Laminar, the other to Virtual DOM like Tyrian and Elm. Both are equally valid choices with quite subtle trade-offs.

Broadly the argument for FRP is speed, as updates are minimal and precise. The argument for Virtual DOM is that it's easier to test and reason about.

However, You don't have to look hard to find counter arguments to both positions: Elm is blazing fast, and Laminar has solved the classic diamond problem. ...but that's the general argument.

In the end, it's mostly personal preference.

## Provenance, and a note of thanks

Tyrian was originally a fork of [Scalm](https://github.com/julienrf/scalm) by [Julien Richard-Foy](https://github.com/julienrf).

Scalm was the Scala.js library I'd been looking for but found too late, and it's great fun! In my opinion it was simply ahead of its time, and alas the original authors and contributors had moved on to pastures new long before it was brought to my attention.

Scalm was forked and re-released it under a new name and licence with the original authors blessing, partly because I wanted to take it in my own direction without corrupting the original work, and partly ...because I just wasn't sure how to pronounce Scalm! (I did ask.)

Scalm/Tyrian and [Indigo](https://github.com/PurpleKingdomGames/indigo) (which I also look after) are kindred spirits, in that they both follow the TEA pattern (The Elm Architecture), which is the only frontend architecture pattern I'm interested in these days.

I hope to use Tyrian to complement Indigo, and so have brought it in under the same organisation.

Tyrian is Scalm with the cobwebs blown off. All it's libraries are up to date, I've started expanding the API, and it will only ever be released against Scala 3 (and beyond!).

With huge thanks to the original authors,

Dave, 5th June 2021

## "Tyrian" Purple

> "It took tens of thousands of desiccated hypobranchial glands, wrenched from the calcified coils of spiny murex sea snails before being dried and boiled, to colour even a single small swatch of fabric, whose fibres, long after staining, retained the stench of the invertebrate's marine excretions. Unlike other textile colours, whose lustre faded rapidly, Tyrian purple ... only intensified with weathering and wear – a miraculous quality that commanded an exorbitant price, exceeding the pigment's weight in precious metals." ~ [BBC](https://www.bbc.com/culture/article/20180801-tyrian-purple-the-regal-colour-taken-from-mollusc-mucus)

So it's a purple dye that smells of where it came from and gets richer over time with use. Perfect.
