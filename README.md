# busymachines-commons

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-core_2.12) [![Sonatype](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.busymachines/busymachines-commons-core_2.12.svg)](https://oss.sonatype.org/#nexus-search;quick~busymachines-commons-core_2.12)  [![Scala](https://img.shields.io/badge/scala-2.12.5-brightgreen.svg)](https://github.com/scala/scala/releases/tag/v2.12.5)

Light-weight, modular eco-system of libraries needed to build http web apps, built _primarily_ on top of the [typelevel.scala](https://github.com/typelevel) ecosystem.

Head over to the microsite for more information:
[http://busymachines.github.io/busymachines-commons/](http://busymachines.github.io/busymachines-commons/)

## Version and language compatibility

Currently built only against Scala `2.12.5`.

Latest version of the library is `0.3.0-RC7`

## Copyright and License

All code is available to you under the Apache 2.0 license, available at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0) and also in the [LICENSE](./LICENSE) file.

## Contributors

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
