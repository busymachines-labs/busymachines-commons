# busymachines-commons-semver-parsers

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver-parsers_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver-parsers_2.12)

## artifacts

This module is vanilla scala _*only*_, cross-compiled against versions: `2.12.4`.

The full module id is:
`"com.busymachines" %% "busymachines-commons-semver-parsers" % "0.2.0-RC5"`

### Transitive dependencies

* [`core`](../core/README.md)
* [`atto`](https://github.com/tpolecat/atto) — parsing library
  * [`cats`](https://github.com/typelevel/cats)

## Description

A companion to the [`semver`](../semver/README.md) module that provides a way to parse plain strings into the semantically meaningful `SemanticVersion` datatype.

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
val stringRep      = version.lowercase // "1.0.0-rc4"
val parsedVersion  = SemanticVersion.unsafeFromString(stringRepr)
// version == parsedVersion
```
