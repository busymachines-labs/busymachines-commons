---
layout: home
---

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12) [![Sonatype](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.busymachines/busymachines-commons-core_2.12.svg)](https://oss.sonatype.org/#nexus-search;quick~busymachines-commons-core_2.12) [![Scala](https://img.shields.io/badge/scala-2.12-brightgreen.svg)](https://github.com/scala/scala/releases/tag/v2.12.10) [![Scala](https://img.shields.io/badge/scala-2.13-brightgreen.svg)](https://github.com/scala/scala/releases/tag/v2.13.1)

# busymachines-commons

If you're interested in programming, head on over to the [Getting Started](docs/) section.

`busymachines-commons` is a light-weight, modular eco-system of libraries needed to build http web apps, built _primarily_ on top of the [typelevel.scala](https://github.com/typelevel) ecosystem.

We strive to bring together everything you need to develop purely functional web-apps in Scala in a user friendly, intuitive way. The design philosophy is a "one import experience" per web specific web-concern, while still preserving as much modularity as possible. We do not propose to reinvent the wheel, rather we aggregate the absolutely brilliant work done by the Scala community and make it digeastable by newcomers. Alongside code examples you will find plenty of resources explaining concepts.

## Credit

The vast majority of the credit goes out to the maintainers and contributors of the open-source libraries we depend upon (in no particular order):
* [cats](https://github.com/typelevel/cats)
* [cats-effect](https://github.com/typelevel/cats-effect)
* [monix](https://github.com/monix/monix)
* [circe](https://github.com/circe/circe)
* [shapeless](https://github.com/milessabin/shapeless)
* [atto-parsers](https://github.com/tpolecat/atto)
* [akka](https://github.com/akka/akka)
* [akka-http](https://github.com/akka/akka-http)
* [akka-http-circe](https://github.com/hseeberger/akka-http-json)
* [scalatest](https://github.com/scalatest)
* [scalacheck](https://github.com/rickynils/scalacheck)
* last but not least, [Scala](https://github.com/scala/scala)

### Contributors

People who have contributed to the new version are:
* @lorandszakacs

#### History

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
