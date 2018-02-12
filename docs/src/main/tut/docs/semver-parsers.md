---
layout: docs
title: semver-parsers
---
[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver-parsers_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver-parsers_2.12)

# busymachines-commons-semver-parsers

## artifacts

* stable: `0.2.0`
* latest: `0.3.0-RC3`

```scala
"com.busymachines" %% "busymachines-commons-semver-parsers" % "0.2.0"
```

### Transitive dependencies

* [core](core.html)
* [atto](https://github.com/tpolecat/atto) — parsing library
  * [cats](https://github.com/typelevel/cats)

## Description

A companion to the [semver](semver.html) module that provides a way to parse plain strings into the semantically meaningful `SemanticVersion` datatype.

## Usage

You are always guaranteed to be able to parse the string representations yielded by the helper methods in the `SemanticVersion` class.

You have four methods found in `SemanticVersionParsers`:
* `busymachines.semver.SemanticVersionParsers.parseSemanticVersion`
* `busymachines.semver.SemanticVersionParsers.parseLabel`
* `busymachines.semver.SemanticVersionParsers.unsafeParseSemanticVersion`
* `busymachines.semver.SemanticVersionParsers.unsafeParseSemanticVersion`

And their counterpart syntactic sugars enabled by importing the `import busymachines.semver.syntax._`
```scala
import busymachines.semver._
import busymachines.semver.syntax._

val version        = SemanticVersion(1,0,0, Labels.rc(4))
val stringRepr     = version.lowercase // "1.0.0-rc4"
val parsedVersion  = SemanticVersion.unsafeFromString(stringRepr)
// version == parsedVersion
```
