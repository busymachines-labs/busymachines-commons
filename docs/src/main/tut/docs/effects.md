---
layout: docs
title: effects
---
[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-effects_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-effects_2.12)

# busymachines-commons-effects

* stable: `N/A`
* latest: `0.3.0-RC10`

```scala
"com.busymachines" %% "busymachines-commons-effects"            % "0.3.0-RC10"
//the above module is actually a composite of the following three modules
"com.busymachines" %% "busymachines-commons-effects-sync"       % "0.3.0-RC10"
"com.busymachines" %% "busymachines-commons-effects-sync-cats"  % "0.3.0-RC10"
"com.busymachines" %% "busymachines-commons-effects-async"      % "0.3.0-RC10"
```

## Recommended usage
```
libraryDependencies += "com.busymachines" %% "busymachines-commons-effects" % "0.3.0-RC10"
```

```scala
import busymachines.effects._
import busymachines.effects.validated._ //only if you need Validated (see below)
```

If you need fine grained control then you can still pick and choose the modules you depend on and the imports you make.

## How it works

The purpose of this module is to give you all the effects (see section in [learning](learning.html#effects)) you need in your daily coding activity, available through a single import:
`import busymachines.effects._`. They [syntax](effects.html#syntax) extensions are thought out in such a way that importing `import cats.implicits._` causes no conflict, and are thought as complementary to `cats`, not a replacement.

The effects are loosely separated into two modules:
* pure, synchronous effects (`sync` module): `Option`, `List`, `Try`, `Either`, `Result`
* impure, asynchronous effects (`async` module): `IO`, `Task`, `Future`

### Gotchas
You have to use either:
```scala
import busymachines.effects.sync._
import busymachines.effects.async._
//or other even more modular imports like:
//import busymachines.effects.sync.result._
```

Or:
```scala
import busymachines.effects._
```

But never combine the latter with any of the former imports. The latter is intended to bring in everything from the former two, and you would be importing certain `val`s twice.

### What effect to use and when. FAQ

##### 1. Do you want to model absence of value or value?

That's an `Option`.

##### 2. Do you want to model zero, or more values?

That's a `List`.

##### 3. Do you want to model absence of value or value but with extra information?

Or, put bluntly, model "failures", "exceptions", or other such things.
That's a `Result`. This can also be achieved with a `Try`, or `Either`, but the usage of the former is discouraged, while the latter is not specialized enough.

##### 4. Do you want to accumulate all errors while processing some data?

That's a `Validated` _Applicative_. Unlike the other effects, this one is not a `Monad`.

##### 5. Does your computation have a side-effect?

All effects in the `sync` should be used for pure operations *only*. Therefore do not, under absolutely any circumstances model "writing a file" as a `Result`, `Try`. That would be absolutely horrible. Use effects that actually are meant to encapsulate side-effects. You should definitely use one of: `IO`, `Task`, `Future`, or some other effect not in here (e.g. slick's [DBIOAction](http://slick.lightbend.com/doc/3.0.0/api/index.html#slick.dbio.DBIOAction))

##### 6. Do you suspect your computation might have side-effects but don't know for sure?

Then use of of the `suspendIn{Eff}` methods to be extra careful.

##### 7. Need concurrency?

Use `Task`. The usage of `Future` is discouraged because of it does not let you manage side-effects at all. See the section in [learning](learning.html#referential-transparency) about it.

### Motivation
The purpose of this module is four-fold.

##### 1. Single import
`import busymachines.effects._`
You can also import only what you need. See [modular imports](effects.html#modular-imports)

##### 2. Use appropriate effect

Encourage properly expressing what your functions do by using the appropriate effect type

##### 3. iron out inconsistencies

Attempts to iron out inconsistencies of the Scala standard library effect type companion objects. E.g. all Effects can be now constructed with a `pure` method on the companion object, and so on. This way, it is a complement to `cats.implicits._`, which provides syntax for:
```scala
import cats.implicits._
val option: Option[String] = "string".pure[Option]
```

```scala
import busymachines.effects._
val option: Option[String] = Option.pure("string")
```

And some other, more useful, operations, detailed in the sections bellow.

##### 4. NOT a monad transformer

This module does not seek to handle cases where all your types look like `IO[Option[T]]`, and you can simply lift those into a `OptionT` and then neatly combine them. If you do have such a scenario then just use monad transformers, it's definitely the best solution out there.

Instead, this module seeks to give fairly uniform syntax for converting from one effect to the other (if possible, see the [conversion](effects.html#conversion-between-effects) section). Since in most extremely large systems we have an `Option` coming from one place, a `Result` from another, `Try` from some legacy code, and you need to combine them all into one final `Task`. In this case you want to simply short circuit everything with a specific [anomaly](core.html) and convert the `Option` into a `Task` immediately and then simply combine other `Task` as usual.

## busymachines-commons-sync

Written entirely in vanilla Scala, provides integration with [core](core.html), and useful syntax for interop for switching between the effects: `Option`, `List`, `Try`, `Either`, `Result`.

N.B. `List` and `Either` have limited support, which will probably be extended in the future.

### Result

The single "novel" contribution of the entire `effects` module is the datatype `Result`, which is defined as:

```scala
type Result[T]    = Either[Anomaly, T]
type Correct[T]   = Right[Anomaly, T]
type Incorrect[T] = Left[Anomaly, T]
```

Used to model "failure". Long story short, it's a specialized `Either`, all semantics are the same. And is a pure, referentially transparent substitute for `Try`, but it also integrates with [core](core.html). There's nothing more to it, you should just use it anytime you have pure computation that can sometimes "fail".

quick-example:
```scala
import busymachines.effects.sync._
//or:
//import busymachines.effects._

val yay: Result[Int] = for {
  x <- Result.pure(42)
  y <- Try.pure(42).asResult
  z <- Option.pure(42).asResult(ifNone = InvalidInputFailure("No such value: 'z'"))
} yield x + y

val nay: Result[Int] = for {
  x <- Result.pure(42)
  y <- Try.pure(42).asResult
  z <- Option.none.asResult(ifNone = InvalidInputFailure("No such value: 'z'"))
} yield x + y + z

val nay2 = for {
  x <- yay
  y <- nay
} yield x + y
```

Standard monadic behavior, leveraging `Either`'s [right bias](https://typelevel.org/cats/datatypes/either.html), but with a fixed type for the left-hand side to considerably reduce the boilerplate for 99% percent of the cases.

## busymachines-commons-sync-cats

This module synthesized cats' `Validated` and [core](core/). You _definitely_ have to read the associated [cats docs](https://typelevel.org/cats/datatypes/validated.html) to properly understand this.

This is a bit of a stray cat compared to the other modules. The reason is that it brings to the table the `Validated` data-type which allows you to accumulate the anomalies of your various effectful computations. The reason that this is slightly off syntax-wise compared to the other modules is because `Validated` is _only_ an `Applicative`, and not also a `Monad`. Meaning that you do not have a `flatMap` method, ergo you cannot use it in `for` comprehensions.

### Validated

Ought to be used as an "anomaly accumulating" version of `Result`, as opposed to having fail first semantics.

The types and syntax are not immediately available with the usual `import busymachines.effects.sync_`, but rather have to be brought in via the `import busymachines.effects.sync.validated._`. This decision was made through empirical observation, the vast majority of time the semantics of `Result` are better suited than `Validated`, and the use of the latter is a special case.

The best examples are in the aforementioned cats docs, and in our tests:
```scala
package busymachines.effects.sync.validation_test

import org.scalatest._

import busymachines.core._
import busymachines.effects.sync._
import busymachines.effects.sync.validated._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 26 Feb 2018
  *
  */
private[validation_test] object PWDValidator {
  private type Password = String

  def apply(s: Password): Validated[Unit] = Validated.sequence_(
    validateSpaces(s),
    validateSize(s)
  )

  private def validateSpaces(s: Password): Validated[Unit] = {
    s.contains(" ").invalidOnTrue(InvSpaces)
  }

  private def validateSize(s: Password): Validated[Unit] = {
    (s.length < 4).invalidOnTrue(InvSize)
  }

  case object InvSpaces extends InvalidInputFailure("cannot contain spaces")
  case object InvSize extends InvalidInputFailure("must have size of at least 4")
}

class ValidatedEffectsTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  private val valid     = "test"
  private val invSpaces = "te st"
  private val invSize   = "te"
  private val invBoth   = "t s"

  describe("validation") {

    test("it should accept valid password") {
      val v = PWDValidator(valid)
      assert(v == Validated.unit)
    }

    test("reject invSpaces") {
      val v = PWDValidator(invSpaces)
      assert(v == Validated.fail(PWDValidator.InvSpaces))
    }

    test("reject invSize") {
      val v = PWDValidator(invSize)
      assert(v == Validated.fail(PWDValidator.InvSize))
    }

    test("reject both") {
      val v = PWDValidator(invBoth)
      assert(v == Validated.fail(PWDValidator.InvSpaces, PWDValidator.InvSize), "failed")

      assert(
        v.asResult == Result.fail(GenericValidationFailures(PWDValidator.InvSpaces, List(PWDValidator.InvSize))),
        "as result — normal"
      )

      assert(
        v.asResult(TestValidationFailures) == Result.fail(TestValidationFailures(PWDValidator.InvSpaces, List(PWDValidator.InvSize))),
        "as result — ctor"
      )
    }

  }

}


```

#### Conversion to other effects

Conversion to other effects forces all `n` `busymachines.core.Anomaly`s accumulated by this `Validated` to be transformed into one single `busymachines.core.Anomalies` type.

Take for instance the previous example from the tests:
```
    test("reject both") {
      val v = PWDValidator(invBoth)
      assert(v == Validated.fail(PWDValidator.InvSpaces, PWDValidator.InvSize), "failed")

      assert(
        v.asResult == Result.fail(GenericValidationFailures(PWDValidator.InvSpaces, List(PWDValidator.InvSize))),
        "as result — normal"
      )

      assert(
        v.asResult(TestValidationFailures) == Result.fail(TestValidationFailures(PWDValidator.InvSpaces, List(PWDValidator.InvSize))),
        "as result — ctor"
      )
    }
```

Where `TestValidationFailures` is defined as:
```scala
package busymachines.effects.sync.validation_test

import busymachines.core._

private[validation_test] case class TestValidationFailures(
  bad:  Anomaly,
  bads: List[Anomaly] = Nil
) extends AnomalousFailures(
      TVFsID,
      s"Test validation failed with ${bads.length + 1} anomalies",
      bad,
      bads
    )

private[validation_test] case object TVFsID extends AnomalyID {
  override def name = "test_validation_001"
}

```

There are always two methods available when converting to other effects:
  1. a no-arg one where we get back all anomalies wrapped in a `busymachines.effects.sync.validated.GenericValidationFailures`
  2. one of the form: `as{Eff}(ctor: (Anomaly, List[Anomaly]) => Anomalies): {Eff}[T]`, which allows you to specify your domain specific anomalies



## busymachines-commons-async

External dependencies:
* [cats-effect](https://github.com/typelevel/cats-effect) — defines `IO`
* [monix](https://github.com/monix/monix) — defines `Task`

The `IO`, `Task` and `Future` types are only aliased here. With syntax extensions as detailed in the next section. Therefore you can easily substitute the original types, and only having to account for the rather thing syntax extensions.

## outline of syntax:

There is quite a lot of it, and you can browse the tests for everything. But here is a rough outline, and the rules to derive what exists. Will use `$F` to denote the corresponding companion object of the effect, e.g. `Result.pure`.

All operations on the companion `$F` have an equivalent operation that deals directly on values of the certain type.

In order to reduce the boilerplate of the bellow documentation, there are several naming conventions that you have to take in mind:
* certain constructors might have extra parameters, or missing ones, depending on the specific effect, even if the functions are called the same. It should be quite obvious what, and why.
* all functions that take an `Anomaly` as a parameter have a method suffixed with the word `Thr` which take a `Throwable` instead. The latter should be used for legacy code that knows nothing about anomalies.
* all extension methods available on a companion object of the form: `$F.method(value: TF, param2: X*)`, have an equivalent on any expression of type `(value: TF).method(param2: X*)`


#### 1. creating effects from pure values

* `$F.pure(t: T): F[T]` — pure function that does not run any side-effects, and lifts your pure value into the corresponding `F` effect. DO NOT use if computing your value causes side-effects (e.g. throwing exceptions, mutating some `var` or worse: a database write)
* `$F.fail(a: Anomaly): F[T]` — represent the failure case of the Effect.
* besides these, where appropriate, there's aliases
* `$F.unit`, all except `Option` and `List` have this.

#### 2. creating effects from other effects

* see section on conversions to see what combinations are possible
* roughly, every companion object has methods like `.from{Effect}`. e.g: `$F.fromOption(opt: Option[T], ifNone: => Anomaly): F[T]`
* and there is infix notation in the for of `$F.as{$G}(...)`  
  * `Option.pure(42).asResult(InvalidInputFailure("No such thing as 42"))`
  * N.B. conversions from `sync` effects to `async` effects are provided only in the `async` module  
* converting back to `Id` using methods `unsafeGet()` or `unsafeSyncGet()` to get back a value of `T` from `F[T]`. DO NOT USE in production code, this is provided as convenience for testing. You have to be a genius to know what the hell you are doing in order to use this properly, and without your program becoming yet another Java program. Pro tip: you're not a genius, be humble.

#### 3. conditional creation
Given a `Boolean`, decide how to create your effect:

* `$F.cond(true, "true branch", InvalidInputFailure("chose the false branch, after all"))`, yields `Correct("true branch")`
* `$F.condWith(true, Result.pure("true branch"), InvalidInputFailure("chose the false branch, after all"))`, yields `Correct("true branch")`
* `false.condResult("true branch", InvalidInputFailure("chose the false branch, after all"))` — unfortunately, in this case, you have to specify the name of the effect as a prefix
* `false.condWithResult(...)`, semantically equivalent to the
* `$F.failOnTrue(true, InvalidInputFailure("I only fail when true"))`
* `true.failOnTrueResult(InvalidInputFailure("I only fail when true"))`
* `$F.failOnFalse(...)`

Given a `Boolean` nested in your effect (`F[Boolean]`), combine it with an effect of the same type `F`. All operations available above, are also available in this form:
* `$F.pure(true).failOnTrue(InvalidInputFailure("I only fail when true"))`

#### 4. special cased F[Option[T]]
Since Option is an extremely wide-spread effect, you want to sometimes "unpack" it into a more powerful effect to continue your work easily, without too much clutter. So we have:
* `val r: F[Int] = $F.pure(Option(42)).unpack(InvalidInputFailure("this was none"))` — fails when the Option is `Some`

#### 4. special cased F[Result[T]]
Since Option is an extremely wide-spread effect, you want to sometimes "unpack" it into a more powerful effect to continue your work easily, without too much clutter. So we have:
* `val r: F[Int] = $F.pure(Result.pure(42)).unpack` — fails with same `anomaly` as the underlying `Result` if it is `Incorrect`

#### 5. $F.traverse + $F.sequence

All companion objects now have ways of lifting a `List[T]` into `F[List[R]]` of T with a function `T => F[R]` via `$F.traverse`. `$F.sequence` is a special case of traverse that transforms `List[F[T]]` into `F[List[T]]`.

`Future`, `IO`, `Task` also have a method called `serialize` which has the same signature as `traverse`, but guarantees that no two computations are run in parallel. In case of `Task` and `IO` it does not matter that much, since `traverse` has the same semantics, but for `Future` it is almost imperative that you use it instead of `traverse` and `sequence` because you can easily queue 600 Futures on your execution context and kill it.

#### 6. `async` specific syntax
Since asynchrony and side-effecting is more complex of a task, the `async` module has the following special operations:

* `expr.suspendIn{IO/Future/Task}` — the expression `effectValue` is passed by name, to ensure that whatever side-effect it has is "suspended" in the specified Effect. This is almost useless when done for `Future` since the side-effect is done on a separate thread, but for `IO` and `Task` it is absolutely invaluable, as it delays side-effects until you explicitely "run" the `IO` or `Task`

```scala
  def writeToDB(i: Int): Unit = ??? // extremely poor imperative code
  val io: IO[Unit] = writeToDB(42).suspendInIO
  //at this point no side-effect has happened!!!

  io.unsafeSyncGet() //applying side-effect, the world is a horrible place
```

```scala
  // slightly, but only slightly, less poor imperative code,
  //see section on referential transparency in "learning materials"
  def writeToDB(i: Int): Future[Unit] = ???
  val io: Task[Unit] = writeToDB(42).suspendInTask
  //at this point no side-effect has happened!!!

  io.unsafeSyncGet() //applying side-effect, the world is a horrible place
```

Apply side-effects in special cases:
* `false.effectOnFalse{$F}: $F[Unit]` — applies effect only when boolean is `false`, a variant for `true` also exists
* `$F.pure(boolean).effectOnFalse` — same as above, but for values wrapped in the given effect `$F`
* `Result.fail(...).effectOnFail{$F}` — applies the specified effect, suspended in the specified effect `$F`, only when the underlying `Result` is `Incorrect`
* `Result.pure(...).effectOnPure{$F}`— the converse of the above
* `Option.fail.effectOnFail{$F}` — same principle as with `Result`
* `Option.pure(...).effectOnPure{$F}`
* `$F.pure(Result.fail(...)).effectOnFail`
* `$F.pure(Result.pure(...)).effectOnPure`
* `$F.pure(Option.fail).effectOnFail`
* `$F.pure(Option.pure(...)).effectOnPure`


```scala
  def diagnose(int: Int): Option[Defect] = ???
  def complexDiagnose(int: Int): Task[Option[Defect]] = ???

  def repair(d: Defect): Task[Unit] = ???

  val repairTask: Task[Unit] = for {
    //unfortunately we are stuck with this small inconsistency in naming due to
    //scala being unable to do proper implicit resolution based on the return type
    _ <- diagnose(42).effectOnPureTask(repair)
    _ <- complexDiagnose(42).effectOnPure(repair)
  } yield ()

  //...
  repairTask.unsafeSyncGet() // apply repair job
```

## conversion between effects

- `List` was elided because no special syntax is provided for it.
- `Either` was elided because it is roughly equivalent to `Result` (the latter being a type alias of `Either`).

Legend:
* `Id` stands for [identity](https://typelevel.org/cats/datatypes/id.html), not "identifier"
* Should be read as: `row element` as `column element`
* `✓` — conversion can be done without loss of semantics. Considered possible even if you need to provide more information on conversion, as happens in the conversion of `Option` to other effects, you have to specify the `Throwable/Anomaly` if the `Option` happens to be `None`.
* `-` — conversion can be done with losing some information. Like `List` to `Option`, you loose all elements except the head of the `List`. In the case of `Future` to `IO` or to `Task` you lose referrential transparency.
* `x` — conversion cannot be done safely, at all, and/or is extremely risky. Conversion functions are sometimes provided but they contain the words `unsafe` and/or `sync`. These functions should never be used outside of testing, and "end of the world" scenarios (once in your `main` method, or when responding to an HTTP request).

#### simplified table (recommended)

Notice how conversions cluster in a diagonal if we enumerate the effects from the ones that capture "fewer" semantics to the ones that capture "more". This is rather intuitive because each of these effects can handle the same basic things (They're all `Applicative` and `Monad`), but then each adds new things to the table. So it's trivial to go from the "weakest" effect (`Id`) to the "strongest" effect (`Task`), but not the other way around. This involves actually handling all concurrency related machinery and applying all side-effects captured in the `Task`; extremely non-trivial things.

Effects |   Id   | Option | Result |   IO   |  Task  |
--------|--------|--------|--------|--------|--------|
   Id   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
 Option |    x   |    ✓   |    ✓   |    ✓   |    ✓   |
 Result |    x   |    -   |    ✓   |    ✓   |    ✓   |
   IO   |    x   |    x   |    x   |    ✓   |    ✓   |
  Task  |    x   |    x   |    x   |    ✓   |    ✓   |

#### complete table

`Try` and `Future` are added in order to interact with legacy code. Writing new modules using them is highly discouraged though.

Effects |   Id   | Option |  Try   | Result |   IO   |  Task  | Future |
--------|--------|--------|--------|--------|--------|--------|--------|
   Id   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
 Option |    x   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
  Try   |    x   |    -   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
 Result |    x   |    -   |    ✓   |    ✓   |    ✓   |    ✓   |    ✓   |
   IO   |    x   |    x   |    x   |    x   |    ✓   |    ✓   |    -   |
  Task  |    x   |    x   |    x   |    x   |    ✓   |    ✓   |    -   |
 Future |    x   |    x   |    x   |    x   |    ✓   |    ✓   |    ✓   |


## modular imports

Each module exposes effect specific imports:

#### effects-sync:
* `import busymachines.effects.sync.option._`
* `import busymachines.effects.sync.tr._`
* `import busymachines.effects.sync.either._`
* `import busymachines.effects.sync.result._`

#### effects-async:
* `import busymachines.effects.async.future._`
* `import busymachines.effects.async.io._`
* `import busymachines.effects.async.task._`

#### effects
* `import busymachines.effects.option._`
* `import busymachines.effects.tr._`
* `import busymachines.effects.either._`
* `import busymachines.effects.result._`
* `import busymachines.effects.future._`
* `import busymachines.effects.io._`
* `import busymachines.effects.task._`
