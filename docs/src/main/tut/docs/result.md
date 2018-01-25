---
layout: docs
title: result
---
[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-result_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-result_2.12)

# busymachines-commons-result

Provides an pure effect type useful for expressing "failure". It is defined as an alias:
```scala
type Result[T] = Either[Anomaly, T]
```

## artifacts

* stable: `N/A`
* latest: `0.3.0-M2`

```scala
"com.busymachines" %% "busymachines-commons-result" % "0.3.0-M2"
```

### Transitive dependencies
- busymachines-commons-core
- cats-effects 0.8.0
- cats-core 1.0.1

## How it works

Provides all necessary syntax to never deal explicitely with the left hand side (unless you really need to) under the `import busymachines.result._`

Documentation still under construction. You can glance over this thing:

```scala
package busymachines.playground

import cats._
import cats.implicits._
import busymachines.core._
import busymachines.result._
import cats.effect._

object ResultMain extends App {
  implicit val anomalyShow: Show[Anomaly] =
    Show.fromToString[Anomaly]

  implicit def resultShow[T](implicit rs: Show[T], as: Show[Anomaly]): Show[Result[T]] = {
    Show.show {
      case Correct(r)   => s"Good(${rs.show(r)})"
      case Incorrect(a) => s"Bad(${as.show(a)})"
    }
  }

  var sideEffect = 0

  val suspendedSideEffect: IO[Int] = Result {
    println("DOING SPOOKY UNSAFE SIDE-EFFECTS BECAUSE I CAN'T PROGRAM PURELY!!")
    sideEffect = 42
    sideEffect
  }.suspendInIO

  if (sideEffect == 42)
    throw CatastrophicError("Side-effects make me sad")

  suspendedSideEffect.unsafeRunSync()
  if (sideEffect != 42)
    throw new RuntimeException("THIS TIME I WANTED SIDE EFFECTS!")

  val correct: Result[String] = Result pure "good" //"adsfsdf".pure[Result]

  val incorrect: Result[String] = Result incorrect InvalidInputFailure("blablabla")

  val catastrophe: Result[Int] =
    Result {
      val x = 42
      if (x == 42) {
        throw new RuntimeException("runtime exception")
      }
      else {
        x
      }
    }

  val anomaly: Result[Int] =
    Result {
      val x = 42
      if (x == 42) {
        throw InvalidInputFailure("anomaly 42")
      }
      else {
        x
      }
    }

  val fromEither = Result.fromEither(
    Left[InvalidInputFailure, String](InvalidInputFailure("sdfsdg"))
  )

  val fromEither2 = Result.fromEither(
    Left[String, String]("zzzzzzzz"),
    (s: String) => InvalidInputFailure(s)
  )

  val x: Result[Unit] = anomaly.bimap(
    identity,
    identity
  ) >> Result.unit

  val fo: Result[Int] =
    Result.fromOption(
      42.some,
      NotFoundFailure("integer not found")
    )

  def fo2 =
    42.some.asResult(
      NotFoundFailure("integer not found take 2")
    )

  val eit =
    Right[Int, String]("42")

  val booleanResultFalse: Result[String] =
    false.cond(
      "Correct",
      InvalidInputFailure("00000")
    )

  val booleanResultTrue =
    true.cond(
      "Correct",
      InvalidInputFailure("1111")
    )

  val failFalse =
    false.failOnFalse(
      InvalidInputFailure("22222")
    )

  val failTrue = true.failOnTrue(
    InvalidInputFailure("33333")
  )

  val future = booleanResultFalse.asFutureAlias

  println {
    show"""
          |
          |good:
          |$correct
          |
          |bad:
          |$incorrect
          |
          |anomaly:
          |$anomaly
          |
          |catastrophe:
          |$catastrophe
          |
          |fromEither:
          |$fromEither
          |
          |fromEither2:
          |$fromEither2
          |
          |fromOption:
          |$fo
          |
          |either:
          |$eit
          |
          |booleanResultFalse
          |$booleanResultFalse
          |
          |booleanResultTrue
          |$booleanResultTrue
          |
          |failFalse
          |$failFalse
          |
          |failTrue
          |$failTrue
          |
        """.stripMargin
  }

}

```