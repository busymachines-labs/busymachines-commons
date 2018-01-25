---
layout: docs
title: Help
---

# Help!

This section tries as much as possible to aggregate all head-scratchers that one usually bumps into when using all the modules in this library

### obtuse implicit resolution compilation errors

Probably due to an missing implicit — probably automatic derivation of some type class. You should just add explicit implicit for one type at a time:
```scala
implicit val x: SomeTypeClass[T] = ???
```
Until you localize the problem — then good luck fixing it.

### compiler/sbt bugs

* compiler crashes with `java.util.NoSuchElementException: key not found: value inst$macro$2`
  * no idea what exactly causes it, but moving code where json codec derivation is done, in a separate file from where the case class definitions are done should do the trick. If not, good luck, lol.
* compilation error `package cats contains object and package with same name: implicits`
  * not this again :( — we thought we solved this one
* `[error] assertion failed: Modified names for busymachines.rest.JsonRestAPITest is empty`
  * or any other file name, just add a statement, or some declaration in the class/trait/object body. Or do a complete purge of your `target` folders and build from scratch. [SBT zinc bug 292](https://github.com/sbt/zinc/issues/292)
