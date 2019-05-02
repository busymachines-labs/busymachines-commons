---
layout: docs
title: Getting Started
---

# Getting Started

Due to the modular nature of the library you can pick and chose modules, if you only need a select few.

## sbt module IDs

```scala
val bmCommonsVersion: String = "0.3.0-RC10"

def bmCommons(m: String): ModuleID = "com.busymachines" %% s"busymachines-commons-$m" % bmCommonsVersion

val bmcCore:          ModuleID = bmCommons("core")                withSources ()
val bmcDuration:      ModuleID = bmCommons("duration")            withSources ()
//if you depend on effects, then no need to bring in the other three
val bmcEffects:       ModuleID = bmCommons("effects")             withSources ()
val bmcEffectsSync:   ModuleID = bmCommons("effects-sync")        withSources ()
val bmcEffectsSyncC:  ModuleID = bmCommons("effects-sync-cats")   withSources ()
val bmcEffectsAsync:  ModuleID = bmCommons("effects-async")       withSources ()
val bmcJson:          ModuleID = bmCommons("json")                withSources ()
val bmcRestCore:      ModuleID = bmCommons("rest-core")           withSources ()
val bmcRestJson:      ModuleID = bmCommons("rest-json")           withSources ()
val bmcSemVer:        ModuleID = bmCommons("semver")              withSources ()
val bmcSemVerParsers: ModuleID = bmCommons("semver-parsers")      withSources ()

val bmcRestJsonTK: ModuleID = bmCommons("rest-json-testkit") % Test withSources ()
val bmcRestCoreTK: ModuleID = bmCommons("rest-core-testkit") % Test withSources ()
```

## modules

Module                             | Description                                                 | Version
---------------------------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------------
[core](core/)                      | semantically relevant exceptions                            | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)
[duration](duration/)              | durations with no special DSL and little bullshit           | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-duration_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-duration_2.12)
[effects-sync](effects/)           | `Result` monad, and helpers for Option, Try, Either!        | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-effects-sync_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-effects-sync_2.12)
[effects-sync-cats](effects/)      | `Validated` applicative, built on top of `cats`!            | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-effects-sync-cats_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-effects-sync-cats_2.12)
[effects-async](effects/)          | easier Scala `Future`, cats `IO`, and monix `Task`          | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-effects-async_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-effects-async_2.12)
[effects](effects/)                | both of the above!                                          | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-effects_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-effects_2.12)
[json](json/)                      | all your json needs!                                        | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12)
[rest-core](rest/)                 | straightforward use of akka-http                            | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core_2.12)
[rest-core-testkit](rest-testkit/) | concise DSL for writing REST level tests                    | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-core-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-core-testkit_2.12)
[rest-json](rest/)                 | json implementation of `rest-core`                          | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json_2.12)
[rest-json-testkit](rest-testkit/) | REST endpoint testing DSL specialized for JSON input/output | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-testkit_2.12)
[semver](semver/)                  | semantic version data-type with natural ordering            | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver_2.12)
[semver-parsers](semver-parsers/)  | text parser for above semantic version                      | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-semver-parsers_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-semver-parsers_2.12)

### deprecated modules

This is a parallel module hierarchy whose json serialization is handled by `spray-json`. DO NOT use together with their non-deprecated counterpart. These will not live very long, use at your own risk. The same design rules were followed, and the `rest` packages are syntactically, and semantically almost identical to the non-deprecated counterparts. Using the `json` package differs the most.

These modules have been removed starting with version `0.3.0-RC10`. In case any bugs are found, they will be fixed and released as version `0.2.x`. Think parallel scala `2.11`, and `2.12` series.

Module                                                                                                                                                | Description                                                 | Version
----------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------------
[json-spray](https://github.com/busymachines/busymachines-commons/tree/68ab320e3e9f56aba0efb518fe687a806de84728/json-spray)                           | `spray` analog of the the `json` module.                    | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json-spray_2.12)
[rest-json-spray](https://github.com/busymachines/busymachines-commons/tree/68ab320e3e9f56aba0efb518fe687a806de84728/rest-json-spray)                 | `spray` analog of `rest-json` module                        | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-spray_2.12)
[rest-json-spray-testkit](https://github.com/busymachines/busymachines-commons/tree/68ab320e3e9f56aba0efb518fe687a806de84728/rest-json-spray-testkit) | `spray` analog of the `rest-json-testkit` module            | [![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-rest-json-spray-testkit_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-rest-json-spray-testkit_2.12)
