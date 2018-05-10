---
layout: docs
title: Learning Materials
---

# Learning Materials

While we strive to make this library aggregate in the most user-friendly way possible advanced concepts in Scala this is by no means enough in order to be proficient at it. Therefore, we suggest the following (free) reading materials. Each an everyone is vitally important, and cannot be skipped, skimmed, or otherwise "disregarded".

Going down the pure functional programming rabbit hole is not easy, and cannot be faked. And when you do have to sacrifice purity, you have to know exactly what you are giving up and why.

### Scala
* this most wonderful and intriguing language
* Dave Gurnell's book ["Essential Scala"](https://underscore.io/training/courses/essential-scala/)
* coursera's ["Functional Programming in Scala Specialization"](https://www.coursera.org/specializations/scala) series of online classes

### functional programming
* Alexandru Nedelcu's blog post ["What is Functional Programming?"](https://alexn.org/blog/2017/10/15/functional-programming.html) neatly summarizez the question, while providing _vital_ references, please follow up on everything in that blog post

### referential transparency
* [referential transparency](https://wiki.haskell.org/Referential_transparency) is one of the cornerstones of functional programming
* [purity](https://en.wikipedia.org/wiki/Pure_function#Pure_functions) is sometimesused interchangeably with "referentially transparent" — justifiably so
* note here is that Scala's standard `Future` is not referentially transparent. Check out [@tpolecat's answer why](https://www.reddit.com/r/scala/comments/3zofjl/why_is_future_totally_unusable/cyns21h/)!
* while you can build decent applications that are almost pure using `Future`, you sacrifice a lot of composability and control over side-effects by doing it
* you should use Scala's `Future` only if you have no other choice, and are forced to by circumstance
* as an alternative to `Future` you should consider something like [monix.eval.Task](https://github.com/monix/monix) or [cats.effect.IO](https://github.com/typelevel/cats-effect). The modules [effects-async](/effects) slightly eases the use of these
* `throw new Exception(...)` is an impure operation! Unless it is immediately captured in a `scala.util.Try`, `cats.effect.IO`, or `monix.eval.Task` you should not use it, and even then be skeptical of what you wrote. Prefer using [Result[T]](/docs/result.html) to model "failure"

### effects
* "effects are good, side-effects are bugs", piece of wisdom from Rob Norris's [scale.bythebay.io 2017 talk](https://www.youtube.com/watch?v=po3wmq4S15A) ~min 17

### side-effects
* the very definition of necessary evil. They do not compose, they can quickly degenerate into spaghetti code that is ridiculously hard to reason about, but we need them because otherwise we'd just be using CPU power for nothing
* the point is to contain them as much as possible, and rely on the good old trick "create a program that describes the side-effects" and then have an "interpreter" apply them
* in the context of web-apps with some sort of HTTP api this means that for your entire request you just build up a program representing what to do in said request, and then, only once, interpret the program to yield the result and send it back over the network

### typeclasses
* [typeclasses](https://blog.scalac.io/2017/04/19/typeclasses-in-scala.html) this is one of the most widely used "design patterns" in Scala
* our `json` module (powered by [circe](https://github.com/circe/circe)) could not work beautifully without it, or serialization/deserialization in general, for that matter

### cats
* [cats](https://github.com/typelevel/cats) stands for "categories"
* we rely on the extended [typelevel](https://github.com/typelevel) ecosystem, of which `cats` is the core
* you should definitely read the book ["Scala with Cats"](https://underscore.io/training/courses/advanced-scala/) by Noel Walsh and David Gurnell

### typeclass derivation
* as a user of this library you don't need to know how to do type-class derivation yourself. But it helps with debugging and avoiding hour-long blocks because you can't get your code to compile
* the de-facto way to do this in Scala is to use [shapeless](https://github.com/milessabin/shapeless)
* an excellent book on shapeless is Dave Gurnell's book ["The Type Astronaut's Guide to Shapeless"](https://underscore.io/training/courses/advanced-shapeless/)
* boiler-plate-less derivation is also routinely done via [def macros](https://docs.scala-lang.org/overviews/macros/overview.html). But the future of this language feature is as of yet slightly uncertain, and should be tracked within the [scalameta](http://scalameta.org/) project. The future de-facto way of doing meta-programming in Scala.

### category theory
* the abstract theory that was cleverly reified into what we now know as "functional programming" today
* quite a good intuition about it can be gained from the "Scala with cats" book from the previous section
* nonetheles, for a better foundation, [Bartosz Milewski's](https://bartoszmilewski.com/) "Category Theory for Programmers" is probably one of the more accessible resources. Available as:
  * [pdf](https://github.com/hmemcpy/milewski-ctfp-pdf)
  * [epub](https://github.com/onlurking/category-theory-for-programmers)
  * [blog posts](https://bartoszmilewski.com/2014/10/28/category-theory-for-programmers-the-preface/)
  * video lecture series: [part 1](https://www.youtube.com/watch?v=I8LbkfSSR58&list=PLbgaMIhjbmEnaH_LTkxLI7FMa2HsnawM_), [part 2](https://www.youtube.com/watch?v=3XTQSx1A3x8&list=PLbgaMIhjbmElia1eCEZNvsVscFef9m0dm)

