# Discussion

If you're new to the territory, we highly recommend [this thoughtful blog post](https://dev.to/raquo/my-four-year-quest-for-perfect-scala-js-ui-development-b9a) by [Laminar](https://laminar.dev/)'s author, Nikita Gazarov.

There is a point in that post where Nikita says the following:

> (..) and having now walked the path myself I finally understood exactly what that reason was: functional reactive programming (FRP) and virtual DOM don't mix!
> 
> Virtual DOM and FRP solve the exact same problem – efficiently keeping the rendered DOM in sync with application state – but they approach it from entirely opposite directions (..)

Quite right too. It's an important fork in the road. One direction takes you to FRP (and Laminar or [Calico](https://www.armanbilge.com/calico/)), the other to Virtual DOM like Tyrian and Elm. Both are equally valid choices with quite subtle trade-offs.

Broadly the argument for FRP is speed, as updates are minimal and precise. The argument for Virtual DOM is that it's easier to test and reason about.

However, You don't have to look hard to find counter arguments to both positions: Elm is blazing fast, and Laminar has solved the classic diamond problem. ...but that's the general argument.

In the end, it's mostly personal preference.
