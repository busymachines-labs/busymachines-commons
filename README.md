# busymachines-commons

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)

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
* stable: `0.1.0` — but almost useless
* latest: `0.2.0-RC5`

These modules are are cross-compiled for Scala versions: `2.12.3`. We try our best to keep them up to date.

#### Modules:
* `"com.busymachines" %% "busymachines-commons-core" % "0.2.0-RC5"`
  * [README.md](/core) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)
* `"com.busymachines" %% "busymachines-commons-json" % "0.2.0-RC5"`
  * [README.md](/json) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json_2.12)
* `"com.busymachines" %% "busymachines-commons-rest-core" % "0.2.0-RC5"`
  * [README.md](/rest-core) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core_2.12)
* `"com.busymachines" %% "busymachines-commons-rest-core-testkit" % "0.2.0-RC5" % Test`
  * [README.md](/rest-core-testkit) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core-testkit_2.12)
* `"com.busymachines" %% "busymachines-commons-rest-json" % "0.2.0-RC5"`
  * [README.md](/rest-json) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json_2.12)
* `"com.busymachines" %% "busymachines-commons-rest-json-testkit" % "0.2.0-RC5" % Test`
  * [README.md](/rest-json-testkit) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-testkit_2.12)

##### deprecated:
This is a parallel module hierarchy whose json serialization is handled by `spray-json`. DO NOT use together with their non-deprecated counterpart. These will not live very long, use at your own risk. The same design rules were followed, and the `rest` packages are syntactically, and semantically almost identical to the non-deprecated counterparts. Using the `json` package differs the most.

* `"com.busymachines" %% "busymachines-commons-json-spray" % "0.2.0-RC5"`
  * [README.md](/json-spray) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json-spray_2.12)
* `"com.busymachines" %% "busymachines-commons-rest-json-spray" % "0.2.0-RC5"`
  * [README.md](/rest-json-spray) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-spray_2.12)
* `"com.busymachines" %% "busymachines-commons-rest-json-spray-testkit" % "0.2.0-RC5" % Test`
  * [README.md](/rest-json-spray-testkit) [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-spray-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-spray-testkit_2.12)

For easy copy-pasting:
```scala
lazy val bmcVersion: String = "0.2.0-RC5"

lazy val bmcCore = "com.busymachines" %% "busymachines-commons-core" % bmcVersion
lazy val bmcJson = "com.busymachines" %% "busymachines-commons-json" % bmcVersion
lazy val bmcRestCore = "com.busymachines" %% "busymachines-commons-rest-core" % bmcVersion
lazy val bmcRestCoreTestkit = "com.busymachines" %% "busymachines-commons-rest-core-testkit" % bmcVersion % Test
lazy val bmcRestJson = "com.busymachines" %% "busymachines-commons-rest-json" % bmcVersion
lazy val bmcRestJsonTestkit = "com.busymachines" %% "busymachines-commons-rest-json-testkit" % bmcVersion % Test

@scala.deprecated("use json module instead", "0.2.0")
lazy val bmJsonSpray = "com.busymachines" %% "busymachines-commons-json-spray" % bmcVersion
@scala.deprecated("use rest-json module instead", "0.2.0")
lazy val bmRestJsonSpray = "com.busymachines" %% "busymachines-commons-rest-json-spray" % bmcVersion
@scala.deprecated("use rest-json-testkit module instead", "0.2.0")
lazy val bmRestJsonSprayTestkit = "com.busymachines" %% "busymachines-commons-rest-json-spray-testkit" % bmcVersion % Test
```

## Library Structure

The idea behind these sets of libraries is to help jumpstart backend RESTful api application servers with varying technology stacks. That's why you will have to pick and choose the modules suited for your specific stack.

Basically, as long as modules reside in the same repository they will be versioned with the same number, and released at the same time to avoid confusion. The moment we realize that a module has to take a life of its own, it will be moved to a separate module and versioned independently.

* [core](/core) `0.1.0`
* [json](/json) `0.2.0-RC5`
* [rest-core](/rest-core) `0.2.0-RC5` - this is an abstract implementation that still requires specific serialization/deserialization
* [rest-core-testkit](/rest-core-testkit) `0.2.0-RC5` - contains helpers that allow testing. Should never wind up in production code.
* [rest-json](/rest-core) `0.2.0-RC5` - used to implement REST APIs that handle JSON
* [rest-json-testkit](/rest-json-testkit) `0.2.0-RC5` - helpers for JSON powered REST APIs


Most likely you don't need to depend on the `rest-core*` modules. But rather on one or more of its reifications like `rest-json`. This separation was done because in the future we might need non-json REST APIs, and then we still want to have a common experience of using `commons`.

### Current version

The latest stable version is `0.1.0`. Modules are guaranteed to be compatible if they share the same version number, otherwise you are taking a serious gamble, and managing modules in any other way is not on the roadmap yet. Up until version `1.0.0` you can expect relatively unstable modules, so use with care.

## Developer's Guide

If you are responsible for publishing this library then you _have_ to follow instructions listed in [z-publishing-artifacts/README.md](z-publishing-artifacts/README.md), otherwise you can simply ignore that folder.

Have throw-away code? Just create a package `playground` anywhere, it is already in `.gitignore` specifically for this purpose. Just make sure it doesn't end up in production/test code :)

### build structure
The build is fairly straightforward. The root folder contains a phantom build from which all modules are configured, and an essentially empty project "busymachines-commons" that is never published, but one that is extremely useful for importing into your IDEs. Most top level folders `X` correspond to the specific `busymachines-commons-X` library. All dependencies are spelled out in the `./build.sbt` file

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
