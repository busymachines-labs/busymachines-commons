# busymachines-commons-semver

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver_2.12)

## artifacts

This module is vanilla scala _*only*_, compiled for scala version `2.12.4`

The full module id is:
`"com.busymachines" %% "busymachines-commons-semver" % "0.2.0"`

### Transitive dependencies
None.

## Description

A simple library containing one type, `SemanticVersion` with a defined natural ordering as defined by the [semantic versioning specification 2.0.0](http://semver.org/spec/v2.0.0.html#spec-item-11).

Currently, the type does not support arbitrary naming of your pre-release labels, but rather constrains you to 4 types: `alpha` < `beta` < `M` (milestone) < `RC` (Release Candidate). In future version an arbitrary label will be added — track [issue 46](https://github.com/busymachines/busymachines-commons/issues/46) for a progress update.

## String representations

If you want to parse plain strings into the `SemanticVersion` type, then use the [`semver-parsers`](../semver-parsers/README.md) module. All stringy representations of `SemanticVersion.lowercase/uppercase/etc...` are parseable by that module.

The case class `SemanticVersion` has 4 string representations that are seen in the "wild" — more or less. Any descriptors apply strictly to the representation of the `label`. The results are equivalent if you only have a semantic version with the non-optional fields: `major`, `minor`, `patch`.

## Ordering
The library is in accordance with the [semantic version specification](http://semver.org/spec/v2.0.0.html#spec-item-11):
```
SemanticVersion(1, 0, 0)                   >
 SemanticVersion(1, 0, 0, Labels.rc(2))     >
  SemanticVersion(1, 0, 0, Labels.rc(1))     >
   SemanticVersion(1, 0, 0, Labels.m(1))      >
    SemanticVersion(1, 0, 0, Labels.beta(1))   >
     SemanticVersion(1, 0, 0, Labels.beta)      >
      SemanticVersion(1, 0, 0, Labels.alpha(1))  >
       SemanticVersion(1, 0, 0, Labels.alpha)     >
        SemanticVersion(1, 0, 0, Labels.snapshot)
```

## Examples of string representations

```scala
import busymachines.semver._
val version = SemanticVersion(1, 0, 0)
// version.lowercase == "1.0.0"
// version.uppercase == "1.0.0"
// version.lowercaseWithDots == "1.0.0"
// version.uppercaseWithDots == "1.0.0"


val snapshot = SemanticVersion(1, 0, 0, Labels.snapshot)
// snapshot.lowercase == "1.0.0-snapshot"
// snapshot.uppercase == "1.0.0-SNAPSHOT"
// snapshot.lowercaseWithDots == "1.0.0-snapshot
// snapshot.uppercaseWithDots == "1.0.0-snapshot


val alphaSingle = SemanticVersion(1, 0, 0, Labels.alpha)
// alphaSingle.lowercase == "1.0.0-alpha"
// alphaSingle.uppercase == "1.0.0-ALPHA"
// alphaSingle.lowercaseWithDots == "1.0.0-alpha"
// alphaSingle.uppercaseWithDots == "1.0.0-ALPHA"

val alpha1 = SemanticVersion(1, 0, 0, Labels.alpha(1))
// alpha1.lowercase == "1.0.0-alpha1"
// alpha1.uppercase == "1.0.0-ALPHA1"
// alpha1.lowercaseWithDots == "1.0.0-alpha.1"
// alpha1.uppercaseWithDots == "1.0.0-ALPHA.1"


val betaSingle = SemanticVersion(1, 0, 0, Labels.beta)
// betaSingle.lowercase == "1.0.0-beta"
// betaSingle.uppercase == "1.0.0-BETA"
// betaSingle.lowercaseWithDots == "1.0.0-beta"
// betaSingle.uppercaseWithDots == "1.0.0-BETA"


val beta1 = SemanticVersion(1, 0, 0, Labels.beta(1))
// betaSingle.lowercase == "1.0.0-beta"
// betaSingle.uppercase == "1.0.0-BETA"
// betaSingle.lowercaseWithDots == "1.0.0-beta"
// betaSingle.uppercaseWithDots == "1.0.0-BETA"


val m1 = SemanticVersion(1, 0, 0, Labels.m(1))
// m1.lowercase == "1.0.0-m1"
// m1.uppercase == "1.0.0-M1"
// m1.lowercaseWithDots == "1.0.0-m.1"
// m1.uppercaseWithDots == "1.0.0-M.1"


val rc1 = SemanticVersion(1, 0, 0, Labels.rc(1))
// rc1.lowercase == "1.0.0-rc1"
// rc1.uppercase == "1.0.0-RC1"
// rc1.lowercaseWithDots == "1.0.0-rc.1"
// rc1.uppercaseWithDots == "1.0.0-RC.1"

val rc1WMeta = SemanticVersion(1, 0, 0, Labels.rc(1), Option("7777"))
// rc1WMeta.lowercase == "1.0.0-rc1+7777"
// rc1WMeta.uppercase == "1.0.0-RC1+7777"
// rc1WMeta.lowercaseWithDots == "1.0.0-rc.1+7777"
// rc1WMeta.uppercaseWithDots == "1.0.0-RC.1+7777"

```
