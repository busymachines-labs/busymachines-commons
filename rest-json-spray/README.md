# busymachines-commons-rest-json-spray

[![Maven Central](https://img.shields.io/maven-central/v/com.busymachines/busymachines-commons-json-spray_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.busymachines/busymachines-commons-json-spray_2.12)

_*DO NOT DEPEND ON BOTH THIS MODULE AND `rest-json`. They share the same packages, and type names. It will end badly, chose one or the other. THIS MODULE WILL RECEIVE WAY LESS ATTENTION THAN THE OTHERS, AND HAS A HIGH CHANCE OF BEING DROPPED.*_

Current version is `0.2.0-RC5`. SBT module id:
`"com.busymachines" %% "busymachines-commons-rest-json-spray" % "0.2.0-RC5"`

You should really, really use the [`rest-json`](../rest-json-spray/README.md) module instead. This one is simply a legacy implementation for supporting the now defunct `spray-json`.

Basically everything is the same as with `rest-json`, except that defining your JSON serializers/deserializers is much more verbose and ugly manner.
