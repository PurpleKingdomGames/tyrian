# Tyrian vs Elm

As you might expect, Elm is a far richer offering that Tyrian is now, and in all likelihood ever will be. It's been around a lot longer, and there are a lot more people working on and with Elm every day feeding into it's design.

Having said that: While Elm's architecture has taken on a life of it's own and influenced the state of the art of functional (and even non-FP!) UI programming, Elm itself has remained somewhat niche.

People tend to love Elm ...or hate it. A lot of that reaction can be attributed to the fact that Elm is a very opinionated language, and you either like that or you do not.

What Elm's opinionated stance buys you is an ecosystem where if your code compiles, it works! And that is an amazing thing! In the cost/benefit analysis: The benefit is an incredibly robust web development experience, at the cost of literally not being able to do anything that is not permitted (because it would break the robustness guarantees of the ecosystem).

Tyrian takes the glorious essence of Elm's architecture, but removes almost all of the opinions ...and with it of course, almost all of the safety nets!

- Want to break up your code into lots of files and classes? Carry on.
- Want to use refined types? Ok then.
- Want to use Typeclasses? No problem.
- Want to bring in a heavyweight FP library? Sure thing.
- Want to talk to JavaScript directly over an FFI? Go nuts.
- Want to do a non-exhaustive match? I mean, you can but...
- Want to throw a massive exception? ...erm ...sure...

Be safe out there folks! 😀
