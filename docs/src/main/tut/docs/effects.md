---
layout: docs
title: effects
---

TODO:
* talk about the conflicts when importing both `busymachines.result/future._;` and `busymachines.effects._`. Need to make it painfully explicit that it's one or the other, never both!


## conversion

`Either` was elided because it is roughly equivalent to `Result` (the latter being a type alias of `Either`).

Should be read as: `row element` to `column element`

Legend:
* `✓` — conversion can be done without loss of semantics. Considered possible even if you need to provide more information on conversion, as happens in the conversion of `Option` to other effects, you have to specify the `Throwable/Anomaly` if the `Option` happens to be `None`.
* `-` — conversion can be done with losing some information. Like `List` to `Option`, you loose all elements except the head of the `List`. In the case of `Future` to `IO` or to `Task` you lose referrential transparency.
* `x` — conversion cannot be done safely, at all, and/or is extremely risky. Conversion functions are sometimes provided but they contain the words `unsafe` and/or `sync`. These functions should never be used outside of testing, and "end of the world" scenarios (once in your `main` method, or when answering an HTTP request).

#### simplified table (recommended)

Effects | Option |  List  | Result |   IO   |  Task  |
--------|--------|--------|--------|--------|--------|
 Option |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
  List  |    -   |    ✓   |    -   |    -   |    -   |
 Result |    x   |    x   |    ✓   |    ✓   |    ✓   |
   IO   |    x   |    x   |    x   |    ✓   |    ✓   |
  Task  |    x   |    x   |    x   |    ✓   |    ✓   |

#### complete table

`Try` and `Future` are added in order to interact with legacy code. Writing new modules using them is highly discouraged, though.

Effects | Option |  List  |  Try   | Result |   IO   |  Task  | Future |
--------|--------|--------|--------|--------|--------|--------|--------|
 Option |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
  List  |    -   |    ✓   |    -   |    -   |    -   |    -   |    -   |
  Try   |    -   |    -   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
 Result |    x   |    x   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
   IO   |    x   |    x   |    x   |    x   |    ✓   |    ✓   |    -   |
  Task  |    x   |    x   |    x   |    x   |    ✓   |    ✓   |    -   |
 Future |    x   |    x   |    x   |    x   |    ✓   |    ✓   |    ✓   |


