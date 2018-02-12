---
layout: docs
title: core
---
[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)

# busymachines-commons-core

Here you find very basic buildings blocks for structuring your exceptions in meaningful ways.

## artifacts

This module is vanilla scala _*only*_, compiled with scala version : `2.12.4`.

* stable: `0.2.0`
* latest: `0.3.0-RC4`

```scala
"com.busymachines" %% "busymachines-commons-core" % "0.2.0"
```

## Description

Currently only contains ways to represent failure of some sort or another. Generally, we are very conservative in what we put here.

## Anomaly (exception) and Catastrophe (error)

There's nothing special about anomaly, and catastrophe, other than the fact they try to represent "pure" failures. They are traits with no pretenses of being thrown around, once they are transformed into a throwable, they become a "Failure".

Why should you use them?
Because they can provide decent failure management out of the box. Use in combination with the `rest` modules, you can move with almost zero effort to meaningful error messages.

The Anomaly trait, the base class for everything in `core`:
```scala
trait Anomaly extends Product with Serializable {
  def id: AnomalyID

  def message: String

  def parameters: Anomaly.Parameters = Anomaly.Parameters.empty

  //important scaladoc elided
  def asThrowable: Throwable
}
```

A simple way of uniquely identifying any failure, together with some useful values to give more context.

This library provides a `DSL` (although it's a bit of a stretch to call it that) to define, and instantiate semantically rich failures.
Look at the scaladoc in [anomaly.scala](src/main/scala/com/busymachines/core/anomaly.scala) for more information.
Essentially we have 6 types of failures (with a plural counterpart to each):

* `NotFoundFailure`
* `UnauthorizedFailure`
* `ForbiddenFailure`
* `DeniedFailure`
* `InvalidInputFailure`
* `ConflictFailure`

Which can be used in two differing styles.

#### Quick failure style

```scala
val option: Option[String] = ??? //...
option.getOrElse(throw NotFoundFailure)
option.getOrElse(throw NotFoundFailure("this specific message, instead of generic"))

```

#### Long term failure style
```scala
object RevolutionaryDomainFailures {
  //create a stable, and unique ID
  case object CannotBeDone extends AnomalyID { val name = "rd_001" }
}

case class SolutionNotFoundFailure(problem: String, attempts: Seq[String]) extends NotFoundFailure(
  s"Solution to problem $problem not found."
) {
  override def id: AnomalyID = RevolutionaryDomainFailures.CannotBeDone

  //you can currently associate (String, String), (String, Seq[String])
  //this compiles because of the implicit conversions in the DSL
  //these implicit conversions can be removed when Dotty comes out
  override def parameters: Anomaly.Parameters = Map(
    "problem" -> problem,
    "attempts" -> attempts
  )
}

object Main {
  //...
  val solutionToPVSNP: Option[Boolean] = None
  solutionToPVSNP.getOrElse(throw SolutionNotFoundFailure("P vs. NP", Seq("1", "2", "3")))

  val solutionToHaltingProblem: Option[Boolean] = None
  solutionToHaltingProblem.getOrElse(throw SolutionNotFoundFailure("Halting Problem", Seq("stop", "12")))
}
```

Similarly, we have predefined errors, but they are not as semantically rich.
