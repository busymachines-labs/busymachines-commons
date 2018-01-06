# busymachines-commons

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12) [![Sonatype](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.busymachines/busymachines-commons-core_2.12.svg)](https://oss.sonatype.org/#nexus-search;quick~busymachines-commons-core_2.12)  

Light-weight, modular, libraries for varying technology stacks, built _primarily_ on top of the [typelevel.scala](https://github.com/typelevel) ecosystem.

## Quickstart

### HELP!

Compiler problems one usually bumps into.

#### obtuse implicit resolution compilation bugs
Probably due to an missing implicit — probably automatic derivation of some type class. You should just add explicit implicit for one type at a time (`implicit val x: SomeTypeClass[T] = ???`) until you localize the problem — then good luck fixing it.

#### compiler/sbt bugs
* compiler crashes with `java.util.NoSuchElementException: key not found: value inst$macro$2`
  * no idea what exactly causes it, but moving code where json codec derivation is done, in a separate file from where the case class definitions are done should do the trick. If not, good luck, lol.
* compilation error `package cats contains object and package with same name: implicits`
  * ow, ffs, not this again :( — I thought I solved this one
* `[error] assertion failed: Modified names for busymachines.rest.JsonRestAPITest is empty`
  * or any other file name, just add a statement, or some declaration in the class/trait/object body. Or do a complete purge of your `target` folders and build from scratch. [SBT zinc bug 292](https://github.com/sbt/zinc/issues/292)

#### versions
* stable: `0.2.0`
* latest: `0.3.0-M1`

These modules compiled for Scala version: `2.12.4`. We try our best to keep them up to date.

#### Modules:
Module | Description | Version
-- | --- | ----
[core](/core) | semantically relevant exceptions | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)
[json](/json) | all your json needs! | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)
[rest-core](/rest-core) | straightforward use of akka-http | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core_2.12)
[`rest-core-testkit`](/rest-core-testkit) | concise DSL for writing REST level tests | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core-testkit_2.12)
[rest-json](/rest-json) | json implementation of `rest-core` | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json_2.12)
[rest-json-testkit](/rest-json-testkit) | REST endpoint testing DSL specialized for JSON input/output | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-testkit_2.12)
[semver](/semver) | semantic version data-type with natural ordering | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver_2.12)
[semver-parsers](/semver-parsers) | text parser for above semantic version | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver-parsers_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver-parsers_2.12)

##### deprecated:
This is a parallel module hierarchy whose json serialization is handled by `spray-json`. DO NOT use together with their non-deprecated counterpart. These will not live very long, use at your own risk. The same design rules were followed, and the `rest` packages are syntactically, and semantically almost identical to the non-deprecated counterparts. Using the `json` package differs the most.

These modules have been removed starting with version `0.3.0-M1`. In case any bugs are found, they will be fixed and released as version `0.2.x`. Think parallel scala `2.11`, and `2.12` series.

Module | Description | Version
--- | --- | ---
[json-spray](/json-spray) | `spray` analog of the the `json` module.  | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json-spray_2.12)
[rest-json-spray](/rest-json-spray) | `spray` analog of `rest-json` module | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-spray_2.12)
[json-spray-testkit](/rest-json-spray-testkit) | `spray` analog of the `rest-json-testkit` module | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-spray-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-spray-testkit_2.12)

For easy copy-pasting:
```scala
val bmcVersion: String = "0.2.0"

val bmcCore            = "com.busymachines" %% "busymachines-commons-core" % bmcVersion
val bmcJson            = "com.busymachines" %% "busymachines-commons-json" % bmcVersion
val bmcRestCore        = "com.busymachines" %% "busymachines-commons-rest-core" % bmcVersion
val bmcRestCoreTestkit = "com.busymachines" %% "busymachines-commons-rest-core-testkit" % bmcVersion % Test
val bmcRestJson        = "com.busymachines" %% "busymachines-commons-rest-json" % bmcVersion
val bmcRestJsonTestkit = "com.busymachines" %% "busymachines-commons-rest-json-testkit" % bmcVersion % Test

val bmcSemVer         = "com.busymachines" %% "busymachines-commons-semver" % bmcVersion
val bmcSemVerParsers  = "com.busymachines" %% "busymachines-commons-semver-parsers" % bmcVersion

```

## Library Structure

The idea behind these sets of libraries is to help jumpstart backend RESTful api application servers with varying technology stacks. That's why you will have to pick and choose the modules suited for your specific stack.

Basically, as long as modules reside in the same repository they will be versioned with the same number, and released at the same time to avoid confusion. The moment we realize that a module has to take a life of its own, it will be moved to a separate module and versioned independently.

* [core](/core) `0.2.0`
* [json](/json) `0.2.0`
* [rest-core](/rest-core) `0.2.0` - this is an abstract implementation that still requires specific serialization/deserialization
* [rest-core-testkit](/rest-core-testkit) `0.2.0` - contains helpers that allow testing. Should never wind up in production code.
* [rest-json](/rest-core) `0.2.0` - used to implement REST APIs that handle JSON
* [rest-json-testkit](/rest-json-testkit) `0.2.0` - helpers for JSON powered REST APIs

Most likely you don't need to depend on the `rest-core*` modules. But rather on one or more of its reifications like `rest-json`. This separation was done because in the future we might need non-json REST APIs, and then we still want to have a common experience of using `commons`.

Other modules:
* [semver](/semver) `0.2.0` - definition of a `SemanticVersion` datatype and its natural ordering according to the [Semantic Version 2.0.0](http://semver.org/) spec. Useful only if you have to manipulate semantic versions in your code. No other modules here depend on it.
* [semver](/semver-parsers) `0.2.0` - parsers from plain string to the above `SemanticVersion`.

### Current version

The latest stable version is `0.2.0`. Modules are guaranteed to be compatible if they share the same version number, otherwise you are taking a serious gamble, and managing modules in any other way is not on the roadmap yet. Up until version `1.0.0` you can expect relatively unstable modules, so use with care.

## Developer's Guide

If you are responsible for publishing this library then you _have_ to follow instructions listed in [z-publishing-artifacts/README.md](z-publishing-artifacts/README.md), otherwise you can simply ignore that folder.

Have throw-away code? Just create a package `playground` anywhere, it is already in `.gitignore` specifically for this purpose. Just make sure it doesn't end up in production/test code :)

### build structure
The build is fairly straightforward. The root folder contains a phantom build from which all modules are configured, and an essentially empty project "busymachines-commons" that is never published, but one that is extremely useful for importing into your IDEs. Most top level folders `X` correspond to the specific `busymachines-commons-X` library. All dependencies are spelled out in the `./build.sbt` file

### scalafmt

This codebase is formatted using [scalafmt](http://scalameta.org/scalafmt/). A simple `sbt scalafmt` formats your entire code. There are plenty of ways of using it with your favorite editor, as well. There's an [IntelliJ plugin](https://plugins.jetbrains.com/plugin/8236-scalafmt).

#### scalafmt IntelliJ gotchas

When using IntelliJ, for some unknown reason, the `.scalafmt.conf` file in the root folder is not picked up in any of the subfolders. This is especially weird since in other multiproject builds this has worked out well. Therefore, I duplicated the `.scalafmt.conf` file in all subfolders, for now. Yey, code duplication!

In `sbt` it works just as expected.

#### scalafmt dangling closing parenthesis ')'

As is pointed out in the fmt config file, not putting a closing parenthesis on a newline when defining/using method params or case class properties one gets this perfect abomination:

```scala
final case class SemanticVersion(major: Int,
                                 minor: Int,
                                 patch: Int,
                                 label: Option[Label] = Option.empty[Label],
                                 meta:  Option[String] = Option.empty[String])
    extends SemanticVersionOrdering with Ordered[SemanticVersion]
```

Instead of the reasonable formatting of:

```scala
final case class SemanticVersion(
  major: Int,
  minor: Int,
  patch: Int,
  label: Option[Label] = Option.empty[Label],
  meta:  Option[String] = Option.empty[String]
) extends SemanticVersionOrdering with Ordered[SemanticVersion]
```


## Contributing

Currently, if you want to contribute use the `fork+pull request` model, and Busy Machines team members will have a look at your PR asap. Currently, the active maintainers of this library are:
* @lorandszakacs

### Contributors

People who have contributed to the new version are:
* @lorandszakacs

### History

This used to be the resting place of the `busymachines-commons` library that we used internally for various projects, it reached version `0.6.5`, but it fell into disrepair. If you require that library, by any chance, then check-out the `zz_deprecated/release-0.6.5` branch, and good luck from there. That bit will never be maintained again. And what you find resembles what was only by accident.

People who have created this history of 935 commits are (listed in order of commits):
* @rditerwich
* @paulsabou
* @mateialexandru
* @lorandszakacs
* @cristiboariu
* @adrianbumbas
* @nfazekas
* @advorkovyy
* @hipjim
* @mihaiSimu
* @scalastuff

You can use github API to get a list of contributors from a public project. At the time of writing you can use:
```
curl -i -H "Accept: application/json" -H "Content-Type: application/json" -X GET https://api.github.com/repos/busymachines/busymachines-commons/contributors  > contributors.json
```
